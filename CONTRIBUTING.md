# Contributing to Sai

Thanks for taking an interest in Sai. This page covers what you need to build the
project and the few conventions that are not obvious from the code.

## Requirements

- JDK 21. The build declares a Java 21 toolchain, so Gradle will provision one if the
  JDK you launch it with is a different version.
- No local Gradle install. Use the wrapper: `./gradlew`.

## Building

```bash
./gradlew build          # compile, test, javadoc, JARs
./gradlew jar            # just the engine JAR
./gradlew clean
```

`./gradlew compileJava` alone does **not** give you a usable engine. The engine's
`@ScriptClass` types are rewritten after compilation by `saigen`, which also generates
their `$Prototype` and `$Constructor` companions. That step writes to
`build/classes/saigen/main` and only the JAR contains the result, so build `jar` (or
anything that depends on it) when you want to run something.

## Testing

```bash
./gradlew test               # Java tests + the nosecurity scripts
./gradlew testOptimistic     # full script corpus, optimistic types on
./gradlew testPessimistic    # full script corpus, optimistic types off
```

The three tasks share `build/sai_code_cache` and each clears it on the way in, so they
are ordered and never run concurrently. `testOptimistic` and `testPessimistic` are where
most of the coverage is - `test` runs only a small script subset.

To narrow a run, pass the selectors rather than editing `build.gradle.kts`:

```bash
./gradlew testOptimistic -Psai.test.roots=test/script/basic
./gradlew testOptimistic -Psai.test.includes='JDK-80*.js'
./gradlew testOptimistic -Psai.test.list=JDK-8006220.js
```

The ECMAScript conformance suite is not part of the default build:

```bash
./gradlew downloadExternals   # shallow-clones tc39/test262 (es5-tests)
./gradlew test262
./gradlew test262Parallel
```

### Script tests

Most tests are JavaScript, not Java. A test is `foo.js` plus an optional
`foo.js.EXPECTED`; the harness runs the script and diffs stdout against the expected
output. Options come from a header comment:

```js
/**
 * @test
 * @run
 * @option -scripting
 */
```

| Directory                     | Runs with                                              |
|-------------------------------|--------------------------------------------------------|
| `test/script/basic`           | file read on `test/script`, `sai.test.*` property read  |
| `test/script/error`           | scripts that must fail to compile; `.EXPECTED` holds the error |
| `test/script/sandbox`         | no special permissions - asserts sandbox containment    |
| `test/script/trusted`         | `AllPermission`                                         |
| `test/script/nosecurity`      | run by `test`                                           |
| `test/script/currently-failing` | excluded from every task - park known-broken tests here |

Tests run under the Turkish locale (`-Duser.language=tr -Duser.country=TR`) on purpose.
Locale-sensitive string operations without an explicit locale behave differently there,
so pin `Locale.ROOT` in any code you add.

## Layout

```
src/main/java         engine sources          org.codelibs.sai.{api,internal,tools}
src/main/resources    .properties, .js, META-INF/services
src/main/javadoc      javadoc overview page
src/test/java         Java tests
src/test/resources    resources the Java tests load
test/script           the JavaScript test corpus
buildtools/saigen     the bytecode post-processor, built as its own source set
samples               example scripts, used by `./gradlew run`
```

`saigen` compiles against the engine's own classes (it reads the annotations and emits
references to the runtime types) and then post-processes them, so it cannot move to
`buildSrc` or an included build.

## Pull requests

- Keep the change focused, and say in the description what you verified.
- Run `./gradlew build testOptimistic testPessimistic` before opening the PR. That is
  exactly what CI runs.
- New behaviour needs a test. For engine changes that usually means a script test with
  a `.EXPECTED` file.
- Do not reformat code you are not otherwise changing.

## Licensing

Sai is GPLv2 with the Classpath Exception, inherited from Nashorn. Keep the existing
copyright headers intact - the licence requires it - and add your own only to files you
create.
