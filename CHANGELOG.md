# Changelog

Notable changes to Sai. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and the project uses [semantic versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Moved the sources to the standard Gradle layout (`src/main/java`, `src/main/resources`,
  `src/test/java`, `src/test/resources`). The JavaScript test corpus stays under `test/`.
- `saigen` no longer rewrites `compileJava`'s output. It reads the compiled classes and
  writes an instrumented tree of its own, which is what the JAR ships, so incremental
  builds and the build cache are now correct.
- All test tasks run against the JAR rather than the class directories.
- The build supports Gradle's configuration cache, and it is on by default.
- Dependency versions moved to `gradle/libs.versions.toml`.
- The build compiles against a Java 21 toolchain instead of `source`/`targetCompatibility`.
- JAR manifest: `Implementation-Title` is now `Sai` and `Implementation-Vendor` is
  `CodeLibs Project`; both still said Oracle. Added `Automatic-Module-Name:
  org.codelibs.sai`.

### Added

- Script test selectors can be set from the command line -
  `-Psai.test.roots`, `-Psai.test.includes`, `-Psai.test.list` - instead of editing the
  build script.
- `CONTRIBUTING.md`, `SECURITY.md`, this changelog, and issue and pull request templates.
- Dependabot configuration for Gradle dependencies and GitHub Actions.

### Removed

- `generateSecurityPolicy`, which produced a policy file no task consumed.
- Ant-era leftovers: `exclude/`, `buildtools/saigen/{build.xml,project.properties,saigen.iml}`,
  and `.jcheck/`.

## [0.3.0] - 2025-10-23

### Changed

- Migrated the build from Ant/Make to Gradle.
- Upgraded ASM from 7.1 to 9.7.
- Publishing moved to the Maven Central Portal Publisher API.

## [0.2.0]

## [0.1.0]

Initial releases, forked from Oracle's Nashorn engine. These predate this changelog; see
the commit history for details.

[Unreleased]: https://github.com/codelibs/sai/compare/sai-0.3.0...HEAD
[0.3.0]: https://github.com/codelibs/sai/releases/tag/sai-0.3.0
