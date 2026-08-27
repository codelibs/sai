CodeLibs Sai
[![Java CI with Gradle](https://github.com/codelibs/sai/actions/workflows/gradle.yml/badge.svg)](https://github.com/codelibs/sai/actions/workflows/gradle.yml)
=============

Sai is a runtime environment for programs written in ECMAScript 5.1 that runs on top of JVM.
This project forked from Nashorn.

## Features

- ECMAScript 5.1 implementation
- Runs on JVM (Java 21+)
- JSR-223 Script Engine API support
- High performance bytecode generation using ASM library

## Usage

### Maven

JAR file is available in Maven repository.
Add the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>org.codelibs</groupId>
    <artifactId>sai</artifactId>
    <version>0.3.0</version>
</dependency>
```

### Gradle

Add the following dependency to your build.gradle:

```gradle
dependencies {
    implementation 'org.codelibs:sai:0.3.0'
}
```

Or in build.gradle.kts:

```kotlin
dependencies {
    implementation("org.codelibs:sai:0.3.0")
}
```

## Build

See [CONTRIBUTING.md](CONTRIBUTING.md) for the project layout, the script test
convention, and how to narrow a test run.

### Requirements

- Java 21. The build declares a Java 21 toolchain, so Gradle provisions one if needed.
- No local Gradle install; use the wrapper.

### Build Commands

```bash
# Full build (compile, test, javadoc, jar)
./gradlew build

# Clean build artifacts
./gradlew clean

# Create JAR only
./gradlew jar

# Run tests
./gradlew test

# Run tests in optimistic mode
./gradlew testOptimistic

# Run tests in pessimistic mode
./gradlew testPessimistic

# Generate Javadoc
./gradlew javadoc

# Generate API Javadoc only
./gradlew javadocApi
```

### Running JavaScript

```bash
# Run a sample JavaScript file
./gradlew run

# Debug mode with code inspection
./gradlew debug
```

### Testing

```bash
# Run all tests
./gradlew test

# Run test262 ECMAScript compliance tests
./gradlew test262

# Run test262 in parallel
./gradlew test262Parallel

# Download external test suites
./gradlew downloadExternals
```

## Build Output

After building, you'll find the following artifacts in `build/libs/`:

- `sai-X.X.X.jar` - Main JAR file
- `sai-X.X.X-sources.jar` - Source code JAR
- `sai-X.X.X-javadoc.jar` - Javadoc JAR

Note that `./gradlew compileJava` on its own does not produce a usable engine: the
`@ScriptClass` types are instrumented after compilation and only the JAR carries the
result. Build `jar`, or anything that depends on it.

## Contributing

Bug reports and pull requests are welcome - see [CONTRIBUTING.md](CONTRIBUTING.md).
For security issues, follow [SECURITY.md](SECURITY.md) rather than opening an issue.

## License

GPLv2 with the Classpath Exception, inherited from Nashorn. See [LICENSE](LICENSE) and
[ASSEMBLY_EXCEPTION](ASSEMBLY_EXCEPTION).
