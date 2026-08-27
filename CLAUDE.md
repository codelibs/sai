# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sai is a runtime environment for programs written in ECMAScript 5.1 that runs on top of the JVM. This project is forked from Oracle's Nashorn JavaScript engine.

**Key Technologies:**
- Java 21, via a Gradle toolchain (`java { toolchain { languageVersion = 21 } }`), so the JDK
  running Gradle does not have to be 21. CI runs JDK 21.
- ECMAScript 5.1 implementation
- ASM bytecode library
- TestNG for testing

## Build Commands

### Gradle Build System

```bash
# Full build (compiles, generates javadoc, runs tests)
./gradlew build

# Clean build artifacts
./gradlew clean

# Compile only
./gradlew compileJava

# Create JAR file
./gradlew jar

# Run tests
./gradlew test

# Run tests in optimistic mode
./gradlew testOptimistic

# Run tests in pessimistic mode
./gradlew testPessimistic

# Generate javadoc for all classes
./gradlew javadoc

# Generate javadoc for API classes only
./gradlew javadocApi

# Publish to Maven Central
./gradlew publish
```

## Testing

The project uses TestNG and has extensive test coverage including:

**Test Modes:**
- Optimistic mode tests
- Pessimistic mode tests
- Security policy tests

**Test Types:**
- Internal API tests
- JSR-223 Script Engine API tests
- External test suites (test262, octane, sunspider)

**Running Tests:**

See Build Commands above for `test` / `testOptimistic` / `testPessimistic`.

```bash
# test262 compliance tests
./gradlew test262

# test262 in parallel
./gradlew test262Parallel

# Download external test suites
./gradlew downloadExternals
```

**Test Location:**
- Java test source: `src/test/java/`
- Java test resources: `src/test/resources/`
- Test scripts: `test/script/`
- External tests: `test/script/external/test262` (shallow-cloned by `./gradlew downloadExternals`,
  branch `es5-tests`)

**Narrowing a script-test run** - pass selectors, do not edit `build.gradle.kts`:

```bash
./gradlew testOptimistic -Psai.test.roots=test/script/basic
./gradlew testOptimistic -Psai.test.includes='JDK-80*.js'
./gradlew testOptimistic -Psai.test.list=JDK-8006220.js
```

## Gotchas

- **Tests run under the Turkish locale** (`-Duser.language=tr -Duser.country=TR` in all three test
  tasks). Locale-sensitive string ops without an explicit locale (`toLowerCase()`, `toUpperCase()`)
  behave differently here - pin `Locale.ROOT`.
- **`./gradlew compileJava` alone does not produce a working engine.** saigen instruments the
  `@ScriptClass` types and generates their `$Prototype`/`$Constructor` companions into
  `build/classes/saigen/main`; only the JAR carries the result. Build `jar`, or anything that
  depends on it. compileJava's own output is pristine and must stay that way - it is what saigen
  reads.
- **Test tasks run against the JAR**, never the class dirs, for the reason above.
- **`copyLibs` looks unused but is not.** `test/script/nosecurity/JDK-8055034.js` forks a Shell
  process whose classpath is built at runtime as `<sai.jar>/../../lib/*`, i.e. `build/lib`.
  Grepping for `build/lib` does not find it.
- **`test`, `testOptimistic` and `testPessimistic` share `build/sai_code_cache`** because
  `CodeStoreAndPathTest` hard-codes it. They are explicitly ordered so they cannot overlap; do not
  remove those `mustRunAfter` declarations.
- **The configuration cache is on** (`org.gradle.configuration-cache=true`). Do not reach for
  `Project` (`file()`, `copy {}`, `project.*`) inside `doFirst`/`doLast` - capture plain values or
  Providers at configuration time instead.
- **`test/script/currently-failing` and `test/script/external` are excluded** from `./gradlew test`.
  Park known-broken tests in `currently-failing`.

## Script Test Convention

Most tests are JavaScript files, not Java. A test is `foo.js` plus an optional `foo.js.EXPECTED`;
the runner executes the script and diffs stdout against the `.EXPECTED` file.

| Directory | Runs with |
|-----------|-----------|
| `test/script/basic` | file-read on `test/script`, `sai.test.*` property read |
| `test/script/error` | scripts that must fail to compile; `.EXPECTED` holds the error text |
| `test/script/sandbox` | no special permissions - asserts sandbox containment |
| `test/script/trusted` | `AllPermission` |
| `test/script/nosecurity` | the subset `./gradlew test` runs by default |

## Architecture

**Source Structure (`src/main/java/org/codelibs/sai/`):**

- `api/` - Public API including JSR-223 scripting interfaces
- `internal/` - Internal implementation:
  - `codegen/` - Bytecode generation and compilation
  - `runtime/` - Runtime environment and core functionality
  - `parser/` - JavaScript parser
  - `dynalink/` - Dynamic linking support
  - `ir/` - Intermediate representation
  - `objects/` - JavaScript object implementations
  - `lookup/`, `scripts/` - method handle lookup and precompiled scripts
- `tools/` - Command-line tools including the Shell

**Code generation:** `buildtools/saigen/src` is a separate `saigen` source set. `runSaigen` reads
`build/classes/java/main` and writes an instrumented tree to `build/classes/saigen/main`, which is
what the JAR ships. saigen compiles against the engine's own classes (it reads the annotations and
emits references to the runtime types), so it cannot move to `buildSrc` or an included build.

**Key Entry Points:**
- Shell tool: `org.codelibs.sai.tools.Shell`
- Saigen code generator: `org.codelibs.sai.internal.tools.saigen.Main`

**Build Artifacts:**
- Main JAR: `build/libs/sai-<version>.jar`
- Compiled classes (pristine): `build/classes/java/main/`
- Instrumented classes (shipped): `build/classes/saigen/main/`
- Test classes: `build/classes/java/test/`

**Configuration:**
- Gradle build: `build.gradle.kts`
- Dependency versions: `gradle/libs.versions.toml`
- Gradle properties: `gradle.properties`
- Project settings: `settings.gradle.kts`
- Meta information: `src/main/resources/META-INF/`

## Running JavaScript

```bash
# Run a JavaScript file
./gradlew run

# Debug mode with code inspection
./gradlew debug
```

The default `run` task executes `samples/test.js` with dump-on-error enabled.
