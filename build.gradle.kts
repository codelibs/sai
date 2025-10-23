/*
 * Sai JavaScript Engine
 * Gradle Build Script
 */

plugins {
    java
    `maven-publish`
    signing
}

// Project Information
group = project.property("group") as String
version = project.property("version") as String

// Java Configuration
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
    withJavadocJar()
}

// Configure sources JAR to handle duplicates
tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Dependency Versions
val asmVersion: String by project
val testngVersion: String by project
val jcommanderVersion: String by project
val bshVersion: String by project
val snakeyamlVersion: String by project

// Source Sets Configuration
sourceSets {
    main {
        java {
            srcDirs("src")
        }
        resources {
            srcDirs("src")
            include("**/*.properties")
            include("**/*.js")
            include("META-INF/**")
            exclude("**/*.java")
        }
    }

    // Saigen tool source set
    create("saigen") {
        java {
            srcDirs("buildtools/saigen/src")
        }
        // Saigen depends on main compiled classes
        compileClasspath += sourceSets.main.get().output + configurations.compileClasspath.get()
        runtimeClasspath += output + sourceSets.main.get().output + configurations.runtimeClasspath.get()
    }

    test {
        java {
            srcDirs("test/src")
        }
        resources {
            srcDirs("test/src", "test/script")
            include("**/*.properties")
            include("**/*.js")
            include("**/*.EXPECTED")
            include("META-INF/**")
            exclude("**/*.java")
        }
    }
}

// Dependencies
repositories {
    mavenCentral()
}

dependencies {
    // ASM bytecode library
    implementation("org.ow2.asm:asm:${asmVersion}")
    implementation("org.ow2.asm:asm-commons:${asmVersion}")
    implementation("org.ow2.asm:asm-util:${asmVersion}")
    implementation("org.ow2.asm:asm-tree:${asmVersion}")
    implementation("org.ow2.asm:asm-analysis:${asmVersion}")

    // Test dependencies
    testImplementation("org.testng:testng:${testngVersion}")
    testImplementation("com.beust:jcommander:${jcommanderVersion}")
    testImplementation("org.beanshell:bsh:${bshVersion}")
    testImplementation("org.yaml:snakeyaml:${snakeyamlVersion}")
}

// Custom Tasks

// Generate version.properties file
val generateVersionProperties = tasks.register("generateVersionProperties") {
    group = "build"
    description = "Generate version.properties file"

    val outputDir = layout.buildDirectory.dir("generated/resources")
    outputs.dir(outputDir)

    doLast {
        val versionFile = outputDir.get().file(
            "org/codelibs/sai/internal/runtime/resources/version.properties"
        ).asFile
        versionFile.parentFile.mkdirs()
        versionFile.writeText("""full=${version}
release=${version}
""")
    }
}

// Process resources including generated version.properties
tasks.processResources {
    dependsOn(generateVersionProperties)
    from(layout.buildDirectory.dir("generated/resources"))
}

// Run saigen tool to generate additional classes
val runSaigen = tasks.register<JavaExec>("runSaigen") {
    dependsOn(tasks.compileJava, tasks.named("compileSaigenJava"))

    group = "build"
    description = "Run saigen to generate additional classes"

    mainClass.set("org.codelibs.sai.internal.tools.saigen.Main")
    classpath = sourceSets["saigen"].runtimeClasspath

    val outputDir = sourceSets.main.get().output.classesDirs.singleFile
    args(
        outputDir.absolutePath,
        "org.codelibs.sai.internal.objects",
        outputDir.absolutePath
    )

    // Output to standard output for debugging
    standardOutput = System.out
    errorOutput = System.err
}

// Ensure saigen runs after compiling main classes but before creating JAR
tasks.classes {
    finalizedBy(runSaigen)
}

// JAR Configuration
tasks.jar {
    dependsOn(runSaigen)

    manifest {
        attributes(
            "Main-Class" to "org.codelibs.sai.tools.Shell",
            "Implementation-Title" to "Oracle Sai",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "Oracle Corporation",
            "Archiver-Version" to "n/a",
            "Build-Jdk" to System.getProperty("java.runtime.version"),
            "Built-By" to "n/a",
            "Created-By" to "Gradle ${gradle.gradleVersion}"
        )
    }
}

// Copy runtime dependencies to build/lib for external Shell processes
val copyLibs = tasks.register<Copy>("copyLibs") {
    group = "build"
    description = "Copy runtime dependencies to build/lib directory"

    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("lib"))
}

// Test Configuration
tasks.test {
    dependsOn(copyLibs)

    useTestNG {
        testLogging.showStandardStreams = true
        listeners.add("org.codelibs.sai.internal.test.framework.JSJUnitReportReporter")
    }

    maxHeapSize = "2G"
    minHeapSize = "2G"

    jvmArgs(
        "-server",
        "-ea",
        "-Dfile.encoding=UTF-8",
        "-Duser.language=tr",
        "-Duser.country=TR"
    )

    // Create build/test directory and copy test resources to Ant-compatible location
    doFirst {
        val buildDir = layout.buildDirectory.get().asFile
        file("$buildDir/test").mkdirs()

        // Copy test resources to build/test/classes for Ant compatibility
        val testResourcesDir = file("$buildDir/resources/test")
        val antTestClassesDir = file("$buildDir/test/classes")
        if (testResourcesDir.exists()) {
            copy {
                from(testResourcesDir)
                into(antTestClassesDir)
            }
        }

        // Clean code cache directory for test isolation
        val codeCacheDir = file("$buildDir/sai_code_cache")
        if (codeCacheDir.exists()) {
            codeCacheDir.deleteRecursively()
        }
    }

    // Add sai.jar to classpath
    classpath = sourceSets.test.get().runtimeClasspath + files(tasks.jar.get().archiveFile)

    // Work from project root
    workingDir = projectDir

    systemProperty("build.dir", layout.buildDirectory.get().asFile.absolutePath)
    systemProperty("test.dir", "test")
    systemProperty("test.js.roots", "test/script/nosecurity")
    systemProperty("test.js.framework", "test/script/assert.js")
    systemProperty("test.basic.dir", "test/script/basic")
    systemProperty("sai.jar", tasks.jar.get().archiveFile.get().asFile.absolutePath)
    systemProperty("sai.version", version.toString())
    systemProperty("sai.fullversion", version.toString())

    // Parser and compiler test properties
    systemProperty("parsertest.verbose", "false")
    systemProperty("parsertest.scripting", "true")
    systemProperty("parsertest.test262", "false")
    systemProperty("compilertest.verbose", "false")
    systemProperty("compilertest.scripting", "true")
    systemProperty("compilertest.test262", "false")

    // Exclude external tests by default
    systemProperty("test.js.exclude.dir", "test/script/currently-failing test/script/external")
    systemProperty("test.js.unchecked.dir", "")

    // Fork JVM options for script tests - include sai.jar and all test runtime dependencies (including TestNG)
    val forkClasspath = (sourceSets.test.get().runtimeClasspath.files + tasks.jar.get().archiveFile.get().asFile)
        .joinToString(File.pathSeparator) { it.absolutePath }
    systemProperty("test.fork.jvm.options", "-Xmx${maxHeapSize} -cp ${forkClasspath}")
}

// Optimistic mode test
val testOptimistic = tasks.register<Test>("testOptimistic") {
    group = "verification"
    description = "Run tests in optimistic mode"

    dependsOn(copyLibs)

    useTestNG()

    maxHeapSize = "2G"
    jvmArgs("-server", "-ea", "-Dfile.encoding=UTF-8", "-Duser.language=tr", "-Duser.country=TR")

    // Create build/test directory and copy test resources
    doFirst {
        val buildDir = layout.buildDirectory.get().asFile
        file("$buildDir/test").mkdirs()

        // Copy test resources to build/test/classes for Ant compatibility
        val testResourcesDir = file("$buildDir/resources/test")
        val antTestClassesDir = file("$buildDir/test/classes")
        if (testResourcesDir.exists()) {
            copy {
                from(testResourcesDir)
                into(antTestClassesDir)
            }
        }

        // Copy test/script directory to build/test/script for .EXPECTED file resolution
        val testScriptDir = file("test/script")
        val buildTestScriptDir = file("$buildDir/test/script")
        if (testScriptDir.exists()) {
            copy {
                from(testScriptDir)
                into(buildTestScriptDir)
                include("**/*.EXPECTED")
            }
        }

        // Clean code cache directory for test isolation
        val codeCacheDir = file("$buildDir/sai_code_cache")
        if (codeCacheDir.exists()) {
            codeCacheDir.deleteRecursively()
        }
    }

    // Add sai.jar to classpath
    classpath = sourceSets.test.get().runtimeClasspath + files(tasks.jar.get().archiveFile)

    // Work from project root
    workingDir = projectDir

    systemProperty("build.dir", layout.buildDirectory.get().asFile.absolutePath)
    systemProperty("optimistic.override", "true")
    systemProperty("test.dir", "test")
    systemProperty("test.js.roots", "test/script/basic test/script/maptests test/script/error test/script/sandbox test/script/trusted")
    systemProperty("test.js.framework", "test/script/assert.js")
    systemProperty("test.basic.dir", "test/script/basic")
    systemProperty("sai.jar", tasks.jar.get().archiveFile.get().asFile.absolutePath)
    systemProperty("sai.version", version.toString())
    systemProperty("sai.fullversion", version.toString())
    systemProperty("parsertest.verbose", "false")
    systemProperty("parsertest.scripting", "true")
    systemProperty("compilertest.verbose", "false")
    systemProperty("compilertest.scripting", "true")

    // Exclude security-related tests (require SecurityManager which is deprecated/removed in Java 17+)
    systemProperty("test.js.exclude.list", "JDK-8010946.js JDK-8020508.js JDK-8031359.js JDK-8043232.js JDK-8055762.js JDK-8067136.js JDK-8068580.js JDK-8137134.js JDK-8158467.js classloader.js javaexceptions.js JDK-8031106.js classbind.js")

    // Fork JVM options for script tests - include sai.jar and all test runtime dependencies
    val forkClasspath = (sourceSets.test.get().runtimeClasspath.files + tasks.jar.get().archiveFile.get().asFile)
        .joinToString(File.pathSeparator) { it.absolutePath }
    systemProperty("test.fork.jvm.options", "-Xmx${maxHeapSize} -cp ${forkClasspath}")

    reports {
        html.outputLocation.set(layout.buildDirectory.dir("reports/tests/optimistic"))
    }
}

// Pessimistic mode test
val testPessimistic = tasks.register<Test>("testPessimistic") {
    group = "verification"
    description = "Run tests in pessimistic mode"

    dependsOn(copyLibs)

    useTestNG()

    maxHeapSize = "2G"
    jvmArgs("-server", "-ea", "-Dfile.encoding=UTF-8", "-Duser.language=tr", "-Duser.country=TR")

    // Create build/test directory and copy test resources
    doFirst {
        val buildDir = layout.buildDirectory.get().asFile
        file("$buildDir/test").mkdirs()

        // Copy test resources to build/test/classes for Ant compatibility
        val testResourcesDir = file("$buildDir/resources/test")
        val antTestClassesDir = file("$buildDir/test/classes")
        if (testResourcesDir.exists()) {
            copy {
                from(testResourcesDir)
                into(antTestClassesDir)
            }
        }

        // Copy test/script directory to build/test/script for .EXPECTED file resolution
        val testScriptDir = file("test/script")
        val buildTestScriptDir = file("$buildDir/test/script")
        if (testScriptDir.exists()) {
            copy {
                from(testScriptDir)
                into(buildTestScriptDir)
                include("**/*.EXPECTED")
            }
        }

        // Clean code cache directory for test isolation
        val codeCacheDir = file("$buildDir/sai_code_cache")
        if (codeCacheDir.exists()) {
            codeCacheDir.deleteRecursively()
        }
    }

    // Add sai.jar to classpath
    classpath = sourceSets.test.get().runtimeClasspath + files(tasks.jar.get().archiveFile)

    // Work from project root
    workingDir = projectDir

    systemProperty("build.dir", layout.buildDirectory.get().asFile.absolutePath)
    systemProperty("optimistic.override", "false")
    systemProperty("test.dir", "test")
    systemProperty("test.js.roots", "test/script/basic test/script/maptests test/script/error test/script/sandbox test/script/trusted")
    systemProperty("test.js.framework", "test/script/assert.js")
    systemProperty("test.basic.dir", "test/script/basic")
    systemProperty("sai.jar", tasks.jar.get().archiveFile.get().asFile.absolutePath)
    systemProperty("sai.version", version.toString())
    systemProperty("sai.fullversion", version.toString())
    systemProperty("parsertest.verbose", "false")
    systemProperty("parsertest.scripting", "true")
    systemProperty("compilertest.verbose", "false")
    systemProperty("compilertest.scripting", "true")

    // Exclude security-related tests (require SecurityManager which is deprecated/removed in Java 17+)
    systemProperty("test.js.exclude.list", "JDK-8010946.js JDK-8020508.js JDK-8031359.js JDK-8043232.js JDK-8055762.js JDK-8067136.js JDK-8068580.js JDK-8137134.js JDK-8158467.js classloader.js javaexceptions.js JDK-8031106.js classbind.js")

    // Fork JVM options for script tests - include sai.jar and all test runtime dependencies
    val forkClasspath = (sourceSets.test.get().runtimeClasspath.files + tasks.jar.get().archiveFile.get().asFile)
        .joinToString(File.pathSeparator) { it.absolutePath }
    systemProperty("test.fork.jvm.options", "-Xmx${maxHeapSize} -cp ${forkClasspath}")

    reports {
        html.outputLocation.set(layout.buildDirectory.dir("reports/tests/pessimistic"))
    }
}

// Generate security policy file
val generateSecurityPolicy = tasks.register("generateSecurityPolicy") {
    group = "build"
    description = "Generate sai.policy file for security tests"

    val policyFile = layout.buildDirectory.file("sai.policy")
    outputs.file(policyFile)

    doLast {
        val jarPath = tasks.jar.get().archiveFile.get().asFile.absolutePath
        val testJarPath = layout.buildDirectory.file("libs/sai-${version}-test.jar").get().asFile.absolutePath

        policyFile.get().asFile.writeText("""
grant codeBase "file:/${jarPath}" {
    permission java.security.AllPermission;
};

grant codeBase "file:/${testJarPath}" {
    permission java.security.AllPermission;
};

grant codeBase "file:/${"$"}{basedir}/test/script/trusted/*" {
    permission java.security.AllPermission;
};

grant codeBase "file:/${"$"}{basedir}/test/script/basic/*" {
    permission java.io.FilePermission "${"$"}{basedir}/test/script/-", "read";
    permission java.io.FilePermission "${"$"}{user.dir}", "read";
    permission java.util.PropertyPermission "user.dir", "read";
    permission java.util.PropertyPermission "sai.test.*", "read";
};
        """.trimIndent())
    }
}

// Javadoc Configuration
tasks.javadoc {
    options {
        (this as StandardJavadocDocletOptions).apply {
            tags("implSpec:a:Implementation Requirements:")
            addStringOption("Xdoclint:-missing", "-quiet")
            encoding = "UTF-8"
            docEncoding = "UTF-8"
            charSet = "UTF-8"
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

    commandLine("git", "clone", "--branch", "es5-tests",
                "https://github.com/tc39/test262", testDir.absolutePath)

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

    dependsOn(downloadTest262)

    useTestNG()
    maxHeapSize = "2G"
    jvmArgs("-server", "-ea")

    // Create build/test directory before running tests
    doFirst {
        file("${layout.buildDirectory.get()}/test").mkdirs()
    }

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

    dependsOn(downloadTest262, tasks.compileTestJava, tasks.jar)

    mainClass.set("org.codelibs.sai.internal.test.framework.ParallelTestRunner")
    classpath = sourceSets.test.get().runtimeClasspath

    maxHeapSize = "2G"
    jvmArgs("-server", "-Dsai.typeInfo.disabled=true")

    // Create build/test directory
    doFirst {
        file("${layout.buildDirectory.get()}/test").mkdirs()
    }

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
