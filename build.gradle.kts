/*
 * Sai JavaScript Engine
 * Gradle Build Script
 */

import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    java
    `maven-publish`
    signing
}

// Project Information
group = project.property("group") as String
version = project.property("version") as String

// Java Configuration
//
// A toolchain rather than source/targetCompatibility, so the build compiles against Java 21
// whatever JDK Gradle itself happens to be running on.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

// Source Sets Configuration
//
// `main` and `test` use the standard Gradle layout (src/main/java, src/main/resources,
// src/test/java, src/test/resources) and need no explicit configuration.
sourceSets {
    // Saigen is a bytecode post-processor that reads the @ScriptClass annotations off the
    // compiled main classes, so it compiles against them. That mutual dependency is why it
    // stays a source set here rather than a separate module or an included build.
    create("saigen") {
        java {
            srcDirs("buildtools/saigen/src")
        }
        compileClasspath += sourceSets.main.get().output + configurations.compileClasspath.get()
        runtimeClasspath += output + sourceSets.main.get().output + configurations.runtimeClasspath.get()
    }

    test {
        resources {
            // The JavaScript test corpus lives outside the standard layout.
            srcDir("test/script")
            include("**/*.properties")
            include("**/*.js")
            include("**/*.EXPECTED")
            include("META-INF/**")
        }
    }
}

// Dependencies
repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.asm)

    testImplementation(libs.testng)
}

// Custom Tasks

// version.properties is read at runtime to report the engine version, so it is generated
// from the project version rather than kept in step by hand.
val generatedResources = layout.buildDirectory.dir("generated/resources")

val generateVersionProperties = tasks.register<WriteProperties>("generateVersionProperties") {
    group = "build"
    description = "Generate version.properties"

    destinationFile = generatedResources.map {
        it.file("org/codelibs/sai/internal/runtime/resources/version.properties")
    }
    property("full", version.toString())
    property("release", version.toString())
}

tasks.processResources {
    dependsOn(generateVersionProperties)
    from(generatedResources)
}

// Saigen post-processing
//
// Saigen instruments the @ScriptClass types in `internal.objects` and emits their
// $Prototype/$Constructor companions. Its Main takes <input-dir> <packages> <output-dir>
// as three separate arguments; passing the same directory for input and output made it
// rewrite compileJava's own output after that task had already reported success, which
// left incremental builds and the build cache unable to reason about the result.
//
// Here it reads the pristine javac output and writes to a tree of its own. That tree
// starts as a full mirror of the compiled classes, so it is a self-contained image of
// exactly what we ship, and compileJava's output is never touched.
val instrumentedClasses = layout.buildDirectory.dir("classes/saigen/main")

val runSaigen = tasks.register<JavaExec>("runSaigen") {
    dependsOn(tasks.compileJava, tasks.named("compileSaigenJava"))

    group = "build"
    description = "Instrument @ScriptClass types and generate their companion classes"

    val pristineClasses = tasks.compileJava.flatMap { it.destinationDirectory }
    val outputDir = instrumentedClasses

    inputs.dir(pristineClasses)
        .withPropertyName("pristineClasses")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(outputDir).withPropertyName("instrumentedClasses")
    outputs.cacheIf { true }

    mainClass.set("org.codelibs.sai.internal.tools.saigen.Main")
    // The output tree is on the classpath as well: saigen verifies each instrumented class
    // with ASM, and that resolves the $Prototype/$Constructor types it writes as it goes.
    // Because that directory is also this task's output, its fingerprint changes once after
    // a clean build, so the first build after `clean` is followed by one extra run of this
    // task before it settles as UP-TO-DATE.
    classpath = sourceSets["saigen"].runtimeClasspath + files(outputDir)

    // Supplied lazily so the absolute paths stay out of the task's input fingerprint.
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(
            pristineClasses.get().asFile.absolutePath,
            "org.codelibs.sai.internal.objects",
            outputDir.get().asFile.absolutePath,
        )
    })

    doFirst {
        // Saigen writes only the classes it rewrites or generates, and requires its output
        // directory to already exist. Seed it with every compiled class so the result is a
        // complete replacement for the javac output.
        val source = pristineClasses.get().asFile.toPath()
        val target = outputDir.get().asFile
        target.deleteRecursively()
        target.mkdirs()
        Files.walk(source).use { paths ->
            paths.forEach { path ->
                val destination = target.toPath().resolve(source.relativize(path))
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination)
                } else {
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }
}

// JAR Configuration
tasks.jar {
    dependsOn(runSaigen)

    // The Java plugin wires main's classes dir in first and duplicates resolve to whichever
    // copy was added first, so the pristine javac output has to be filtered out by source
    // location for the saigen-instrumented tree to be the one that ships.
    val pristineClassesPath = tasks.compileJava.get().destinationDirectory.get().asFile.absolutePath
    exclude { it.file.absolutePath.startsWith(pristineClassesPath) }
    from(instrumentedClasses)

    manifest {
        attributes(
            "Main-Class" to "org.codelibs.sai.tools.Shell",
            "Implementation-Title" to "Sai",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "CodeLibs Project",
            // Fixes the module name for consumers on the module path, ahead of any real
            // module-info. Changing it later would break them, so it is set now.
            "Automatic-Module-Name" to "org.codelibs.sai",
            "Build-Jdk" to System.getProperty("java.runtime.version"),
            "Created-By" to "Gradle ${gradle.gradleVersion}"
        )
    }
}

// test/script/nosecurity/JDK-8055034.js forks a Shell process and builds its classpath as
// `<sai.jar>/../../lib/*`, which resolves to build/lib. The path is assembled at runtime, so
// grepping for "build/lib" does not find this use - do not remove this task.
val copyLibs = tasks.register<Copy>("copyLibs") {
    group = "build"
    description = "Stage runtime dependencies in build/lib for forked Shell processes"

    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("lib"))
}

// The engine under test is the JAR. Only the JAR carries the saigen-instrumented classes,
// so the pristine `main` classes dir must stay off the test classpath - on it, javac's
// uninstrumented copies would shadow the ones we actually ship.
fun engineTestClasspath(): FileCollection =
    sourceSets.test.get().output +
        files(tasks.jar) +
        configurations.testRuntimeClasspath.get()

// SourceTest reads a resource from build/test/classes, and the harness writes each script's
// .OUTPUT/.ERROR under build/test, so this has to be in place before any test runs.
val stageTestResources = tasks.register<Sync>("stageTestResources") {
    group = "verification"
    description = "Stage test resources where the script test harness looks for them"

    from(tasks.processTestResources)
    into(layout.buildDirectory.dir("test/classes"))
}

// `test` runs the Java tests plus the scripts that need no security manager;
// testOptimistic/testPessimistic run the full script corpus.
val nosecurityScriptRoots = "test/script/nosecurity"
val fullScriptRoots =
    "test/script/basic test/script/maptests test/script/error test/script/sandbox test/script/trusted"

// These need a SecurityManager, which is disabled by default from Java 18 on.
val securityManagerTests =
    "JDK-8010946.js JDK-8020508.js JDK-8031359.js JDK-8043232.js JDK-8055762.js " +
        "JDK-8067136.js JDK-8068580.js JDK-8137134.js JDK-8158467.js classloader.js " +
        "javaexceptions.js JDK-8031106.js classbind.js"

/**
 * Wiring shared by every script-driven test task.
 *
 * The roots/includes/list selectors can be overridden from the command line, so narrowing a
 * run does not mean editing this file:
 *
 *   ./gradlew testOptimistic -Psai.test.roots=test/script/basic
 *   ./gradlew testOptimistic -Psai.test.includes=let.js
 *   ./gradlew testOptimistic -Psai.test.list="test/script/basic/es6/let.js test/script/basic/es6/const.js"
 *
 * includes is matched with String.endsWith against the file name, not as a glob, and list
 * takes paths relative to the project root, separated by spaces.
 */
fun Test.configureScriptTests(defaultRoots: String) {
    dependsOn(stageTestResources, copyLibs)

    maxHeapSize = "2G"
    jvmArgs("-server", "-ea", "-Dfile.encoding=UTF-8", "-Duser.language=tr", "-Duser.country=TR")

    classpath = engineTestClasspath()
    // Custom Test tasks stop inheriting this by convention in Gradle 9.
    testClassesDirs = sourceSets.test.get().output.classesDirs
    workingDir = projectDir

    val buildDirPath = layout.buildDirectory.get().asFile.absolutePath
    val saiJarPath = tasks.jar.flatMap { it.archiveFile }.get().asFile.absolutePath
    val codeCacheDir = layout.buildDirectory.dir("sai_code_cache").get().asFile

    // A run has to start from an empty code cache or it can pick up another run's classes.
    doFirst {
        codeCacheDir.deleteRecursively()
    }

    systemProperty("build.dir", buildDirPath)
    systemProperty("test.dir", "test")
    systemProperty("test.js.framework", "test/script/assert.js")
    systemProperty("test.basic.dir", "test/script/basic")
    systemProperty("sai.jar", saiJarPath)
    systemProperty("sai.version", version.toString())
    systemProperty("sai.fullversion", version.toString())

    systemProperty("parsertest.verbose", "false")
    systemProperty("parsertest.scripting", "true")
    systemProperty("compilertest.verbose", "false")
    systemProperty("compilertest.scripting", "true")

    systemProperty(
        "test.js.roots",
        providers.gradleProperty("sai.test.roots").getOrElse(defaultRoots),
    )
    providers.gradleProperty("sai.test.includes").orNull?.let {
        systemProperty("test.js.includes", it)
    }
    providers.gradleProperty("sai.test.list").orNull?.let {
        systemProperty("test.js.list", it)
    }

    // The script tests fork a JVM of their own, which needs the same engine on its classpath.
    val forkClasspath = engineTestClasspath().files
        .joinToString(File.pathSeparator) { it.absolutePath }
    systemProperty("test.fork.jvm.options", "-Xmx${maxHeapSize} -cp ${forkClasspath}")
}

// Test Configuration
tasks.test {
    useTestNG {
        testLogging.showStandardStreams = true
        listeners.add("org.codelibs.sai.internal.test.framework.JSJUnitReportReporter")
    }

    configureScriptTests(nosecurityScriptRoots)

    minHeapSize = "2G"

    systemProperty("parsertest.test262", "false")
    systemProperty("compilertest.test262", "false")
    systemProperty("test.js.exclude.dir", "test/script/currently-failing test/script/external")
    systemProperty("test.js.unchecked.dir", "")
}

// Optimistic typing is the engine's default. Both settings are exercised so a regression in
// either code path shows up.
fun registerModeTest(name: String, reportDir: String, optimistic: Boolean) =
    tasks.register<Test>(name) {
        group = "verification"
        description = "Run the script test suite with optimistic types ${if (optimistic) "on" else "off"}"

        useTestNG()
        configureScriptTests(fullScriptRoots)

        systemProperty("optimistic.override", optimistic.toString())
        systemProperty("test.js.exclude.list", securityManagerTests)

        reports {
            html.outputLocation.set(layout.buildDirectory.dir("reports/tests/$reportDir"))
        }
    }

val testOptimistic = registerModeTest("testOptimistic", "optimistic", optimistic = true)
val testPessimistic = registerModeTest("testPessimistic", "pessimistic", optimistic = false)

// CodeStoreAndPathTest hard-codes build/sai_code_cache, so all three tasks read and write one
// shared directory and each clears it before it starts. Overlapping runs would corrupt each
// other's cache, so keep them strictly ordered whenever more than one is scheduled.
testOptimistic.configure { mustRunAfter(tasks.test) }
testPessimistic.configure { mustRunAfter(tasks.test, testOptimistic) }

// Javadoc Configuration
tasks.javadoc {
    options {
        (this as StandardJavadocDocletOptions).apply {
            tags("implSpec:a:Implementation Requirements:")
            addStringOption("Xdoclint:-missing", "-quiet")
            encoding = "UTF-8"
            docEncoding = "UTF-8"
            charSet = "UTF-8"
            overview = "src/main/javadoc/overview.html"
        }
    }

    source = sourceSets.main.get().allJava
    classpath = configurations.compileClasspath.get()
}

// API-only Javadoc
val javadocApi = tasks.register<Javadoc>("javadocApi") {
    group = "documentation"
    description = "Generate Javadoc for API classes only"

    options {
        (this as StandardJavadocDocletOptions).apply {
            tags("implSpec:a:Implementation Requirements:")
            addStringOption("Xdoclint:-missing", "-quiet")
            encoding = "UTF-8"
        }
    }

    source = sourceSets.main.get().allJava
    include("org/codelibs/sai/api/**/*.java")
    classpath = configurations.compileClasspath.get()
    destinationDir = layout.buildDirectory.dir("docs/javadoc-api").get().asFile
}

// External test suites download
val downloadTest262 = tasks.register<Exec>("downloadTest262") {
    group = "external"
    description = "Download test262 test suite"

    val testDir = file("test/script/external/test262")

    // es5-tests is a frozen legacy branch, so a shallow clone is enough and saves most of
    // the download.
    commandLine(
        "git", "clone", "--depth", "1", "--branch", "es5-tests",
        "https://github.com/tc39/test262", testDir.absolutePath,
    )

    onlyIf { !testDir.exists() }
}

val downloadExternals = tasks.register("downloadExternals") {
    group = "external"
    description = "Download all external test suites"

    dependsOn(downloadTest262)
}

// Test262 test
val test262 = tasks.register<Test>("test262") {
    group = "verification"
    description = "Run test262 ECMAScript compliance tests"

    dependsOn(downloadTest262, stageTestResources)

    useTestNG()
    maxHeapSize = "2G"
    jvmArgs("-server", "-ea")

    classpath = engineTestClasspath()
    testClassesDirs = sourceSets.test.get().output.classesDirs

    systemProperty("build.dir", layout.buildDirectory.get().asFile.absolutePath)
    systemProperty("test.dir", "test")
    systemProperty("test.js.roots", "test/script/external/test262/test/suite")
    systemProperty("test.js.shared.context", "true")
    systemProperty("test.js.enable.strict.mode", "true")
    systemProperty("test.js.exclude.dir", "test/script/external/test262/test/suite/intl402/ test/script/external/test262/test/suite/bestPractice/")
    systemProperty("test.js.framework", "test/script/test262.js test/script/external/test262/test/harness/framework.js test/script/external/test262/test/harness/sta.js")
    systemProperty("sai.jar", tasks.jar.get().archiveFile.get().asFile.absolutePath)
}

// Test262 parallel
val test262Parallel = tasks.register<JavaExec>("test262Parallel") {
    group = "verification"
    description = "Run test262 tests in parallel"

    dependsOn(downloadTest262, tasks.compileTestJava, tasks.jar, stageTestResources)

    mainClass.set("org.codelibs.sai.internal.test.framework.ParallelTestRunner")
    classpath = engineTestClasspath()

    maxHeapSize = "2G"
    jvmArgs("-server", "-Dsai.typeInfo.disabled=true")

    systemProperty("build.dir", layout.buildDirectory.get().asFile.absolutePath)
    systemProperty("test.dir", "test")
    systemProperty("test.js.roots", "test/script/external/test262/test/suite")
    systemProperty("test.js.shared.context", "true")
    systemProperty("test.js.enable.strict.mode", "true")
    systemProperty("test.js.exclude.dir", "test/script/external/test262/test/suite/intl402/ test/script/external/test262/test/suite/bestPractice/")
    systemProperty("sai.jar", tasks.jar.get().archiveFile.get().asFile.absolutePath)
}

// Publishing Configuration
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Sai JavaScript Engine")
                description.set("JavaScript engine developed in the Java programming language based on ECMAScript 5.1")
                url.set("https://github.com/codelibs/sai")
                inceptionYear.set("2012")

                licenses {
                    license {
                        name.set("The GNU General Public License, version 2, with the Classpath Exception")
                        url.set("https://raw.githubusercontent.com/codelibs/sai/master/LICENSE")
                    }
                }

                organization {
                    name.set("CodeLibs Project")
                    url.set("https://www.codelibs.org/")
                }

                developers {
                    developer {
                        id.set("shinsuke")
                        name.set("Shinsuke Sugaya")
                        email.set("shinsuke.sugaya@codelibs.co")
                        organization.set("CodeLibs Inc.")
                        organizationUrl.set("https://codelibs.co")
                    }
                }

                scm {
                    connection.set("scm:git:git@github.com:codelibs/sai.git")
                    url.set("https://github.com/codelibs/sai")
                    developerConnection.set("scm:git:git@github.com:codelibs/sai.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "central"
            val isSnapshot = version.toString().endsWith("-SNAPSHOT")

            url = if (isSnapshot) {
                uri("https://central.sonatype.com/repository/maven-snapshots/")
            } else {
                uri("https://central.sonatype.com/api/v1/publish")
            }

            credentials {
                username = project.findProperty("mavenCentralUsername")?.toString() ?: System.getenv("MAVEN_CENTRAL_USERNAME")
                password = project.findProperty("mavenCentralPassword")?.toString() ?: System.getenv("MAVEN_CENTRAL_PASSWORD")
            }
        }
    }
}

// Signing Configuration
signing {
    // Configure signing from environment variables if not in gradle.properties
    val signingKeyId = project.findProperty("signing.keyId")?.toString()
        ?: System.getenv("SIGNING_KEY_ID")
    val signingPassword = project.findProperty("signing.password")?.toString()
        ?: System.getenv("SIGNING_PASSWORD")
    val signingSecretKeyRingFile = project.findProperty("signing.secretKeyRingFile")?.toString()
        ?: System.getenv("SIGNING_SECRET_KEY_RING_FILE")

    // Use in-memory key if provided
    val signingKey = project.findProperty("signing.key")?.toString()
        ?: System.getenv("SIGNING_KEY")

    val hasSigningConfig = if (signingKey != null && signingPassword != null) {
        // In-memory key configuration
        useInMemoryPgpKeys(signingKey, signingPassword)
        true
    } else if (signingKeyId != null && signingPassword != null && signingSecretKeyRingFile != null) {
        // Traditional keyring file configuration
        // Note: Gradle's signing plugin will use these from project properties automatically
        // We just need to ensure they are set
        if (!project.hasProperty("signing.keyId")) {
            project.extra["signing.keyId"] = signingKeyId
        }
        if (!project.hasProperty("signing.password")) {
            project.extra["signing.password"] = signingPassword
        }
        if (!project.hasProperty("signing.secretKeyRingFile")) {
            project.extra["signing.secretKeyRingFile"] = signingSecretKeyRingFile
        }
        true
    } else {
        false
    }

    isRequired = hasSigningConfig

    if (hasSigningConfig) {
        sign(publishing.publications["maven"])
    }
}

// Custom run task for testing
val run = tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the Shell with a sample script"

    dependsOn(tasks.jar)

    mainClass.set("org.codelibs.sai.tools.Shell")
    classpath = files(tasks.jar.get().archiveFile) + configurations.runtimeClasspath.get()
    workingDir = file("samples")

    args("-dump-on-error", "test.js")

    maxHeapSize = "2G"
    jvmArgs("-server")
}

// Debug run task
val debug = tasks.register<JavaExec>("debug") {
    group = "application"
    description = "Debug the Shell with code inspection enabled"

    dependsOn(tasks.jar)

    mainClass.set("org.codelibs.sai.tools.Shell")
    classpath = files(tasks.jar.get().archiveFile) + configurations.runtimeClasspath.get()
    workingDir = file("samples")

    args(
        "--print-code",
        "--verify-code",
        "--print-symbols",
        "test.js"
    )

    maxHeapSize = "2G"
    jvmArgs("-server", "-Dsai.codegen.debug=true")
}
