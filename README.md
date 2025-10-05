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
    <version>0.3.0-SNAPSHOT</version>
</dependency>
```

### Gradle

Add the following dependency to your build.gradle:

```gradle
dependencies {
    implementation 'org.codelibs:sai:0.3.0-SNAPSHOT'
}
```

Or in build.gradle.kts:

```kotlin
dependencies {
    implementation("org.codelibs:sai:0.3.0-SNAPSHOT")
}
```

## Build

### Requirements

- Java 21 or later
- Gradle 8.x (included via wrapper)

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
