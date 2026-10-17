# AGENTS.md - grails-jasypt-encryption

## Project Overview

The Grails Jasypt Encryption plugin provides strong field-level encryption support on Grails
GORM `String` fields, built on Jasypt and Bouncy Castle.

- **Language:** Groovy 5.1.3 on Java 21
- **Framework:** Grails 8.x
- **Build System:** Gradle 9.8.1 (with wrapper)
- **Current Version:** 6.0.0-SNAPSHOT
- **License:** Apache 2.0

## Skill Files (Best Practices)

Detailed best practices are documented in `.skills/`:

| Skill File                                                             | Purpose                                               |
|------------------------------------------------------------------------|-------------------------------------------------------|
| [`.skills/repository-structure.md`](.skills/repository-structure.md)   | Canonical directory layout and architectural rules    |
| [`.skills/gradle-best-practices.md`](.skills/gradle-best-practices.md) | Gradle best practices, convention plugins, and idioms |
| [`.skills/plugin-project.md`](.skills/plugin-project.md)               | Plugin project scope: source code + unit tests only   |
| [`.skills/example-apps.md`](.skills/example-apps.md)                   | Example app patterns: integration & functional tests  |

**Read these skill files before making structural changes to the repository.**

## Critical Rules

1. **NEVER add code to the root `build.gradle` to configure subprojects.** No `subprojects {}`, `allprojects {}`, or
   `configure()` blocks. All shared configuration goes through convention plugins in `build-logic/`.
2. **The plugin projects contain ONLY plugin code and unit tests.** No integration tests, no functional tests, no
   example controllers or views.
3. **Example apps under `examples/` host all integration and functional tests.** They depend on the plugin via
   `implementation project(':grails-jasypt-encryption-hibernate5')` (or `-hibernate7`) and test it as a real
   consumer would.
4. **Use Gradle convention plugins to deduplicate.** If two or more subprojects share build logic, extract it into a
   convention plugin in `build-logic/`.
5. **Always use lazy Gradle APIs** to avoid eager initialization (`tasks.register()`, `tasks.named()`, `configureEach`,
   `provider {}`).

## Repository Structure

```
grails-jasypt/
├── .skills/             # Best practice skill files
├── plugins/
│   ├── core/            # Grails plugin + shared config (artifact: grails-jasypt-encryption-core)
│   ├── hibernate5/      # Hibernate 5 user types (artifact: grails-jasypt-encryption-hibernate5)
│   ├── hibernate7/      # Hibernate 7 user types (artifact: grails-jasypt-encryption-hibernate7)
│   └── relocation/      # Relocation POM from the pre-6.0 artifact grails-jasypt-encryption to -hibernate5
├── examples/
│   ├── sample-hibernate5/  # Example Grails app on Hibernate 5, with the integration tests
│   └── sample-hibernate7/  # Runs the sources of sample-hibernate5 on Hibernate 7
├── docs/                # Asciidoctor documentation
├── build-logic/         # Gradle convention plugins (composite build)
├── .github/workflows/   # CI, release, and release-notes workflows
├── build.gradle         # Root build file (docs + root-publish ONLY)
├── settings.gradle      # Multi-project settings
└── gradle.properties    # Version properties
```

## Build and Test Commands

```bash
# Full build (compile + test)
./gradlew build

# Run only unit tests (plugin modules)
./gradlew :grails-jasypt-encryption-core:test :grails-jasypt-encryption-hibernate5:test :grails-jasypt-encryption-hibernate7:test

# Run integration tests (example apps)
./gradlew :sample-hibernate5:integrationTest :sample-hibernate7:integrationTest

# Skip tests
./gradlew build -PskipTests

# Run the example app
./gradlew :sample-hibernate5:bootRun

# Generate documentation
./gradlew docs

# Clean build
./gradlew clean build

# Run code style checks only
./gradlew codeStyle

# Skip code style checks
./gradlew build -PskipCodeStyle
```

## SDK Requirements

Use SDKMAN to install the correct tool versions (see `.sdkmanrc`):

- Java: `21.0.12-librca`
- Gradle: `9.8.1`
- Groovy: `5.1.3`

Run `sdk env install` to set up the environment.

## Architecture

Grails 8 supports both Hibernate 5 and Hibernate 7, so the plugin is split into three published modules:

1. **`grails-jasypt-encryption-core`** contains the Grails plugin descriptor `JasyptEncryptionGrailsPlugin`
   (registers the Bouncy Castle provider) and `JasyptDefaultParameters`, which reads the `jasypt` config
   namespace. It exposes `jasypt`, `jasypt-hibernate5` (only its Hibernate independent encryptor registry and
   parameter names are used) and Bouncy Castle as API dependencies.
2. **`grails-jasypt-encryption-hibernate5`** wraps the jasypt-hibernate5 `UserType`s in
   `JasyptConfiguredUserType` subclasses (`GormEncryptedStringType`, `GormEncryptedBigDecimalType`, etc.).
3. **`grails-jasypt-encryption-hibernate7`** implements the same `GormEncrypted*Type` classes (same names
   and package) on the Hibernate 7 `UserType` API, as jasypt has no Hibernate 7 support. They must store
   values in the same format as the Hibernate 5 types, so apps can switch without re-encrypting data.

`plugins/relocation` publishes only a POM for `grails-jasypt-encryption` (the artifact name up to version 5),
relocating it to `grails-jasypt-encryption-hibernate5`.

### Core Classes

| Class / Interface              | Location                                                  | Purpose                                                        |
|--------------------------------|-----------------------------------------------------------|----------------------------------------------------------------|
| `JasyptEncryptionGrailsPlugin` | `plugins/core/src/main/groovy/jasypt/encryption/`         | Plugin descriptor; registers the Bouncy Castle provider        |
| `JasyptDefaultParameters`      | `plugins/core/src/main/groovy/com/bloomhealthco/jasypt/`  | Default user type parameters from the `jasypt` config          |
| `JasyptConfiguredUserType`     | `plugins/hibernate5/src/main/groovy/com/bloomhealthco/jasypt/` | Base class wiring a jasypt-hibernate5 `UserType` to the config |
| `AbstractEncryptedUserType`    | `plugins/hibernate7/src/main/groovy/com/bloomhealthco/jasypt/` | Base class of the Hibernate 7 user types                       |
| `JasyptEncryptorParameters`    | `plugins/hibernate7/src/main/groovy/com/bloomhealthco/jasypt/` | Creates the jasypt encryptors like jasypt-hibernate5 does      |
| `GormEncrypted*Type`           | `plugins/hibernate{5,7}/src/main/groovy/com/bloomhealthco/jasypt/` | Per-type GORM mappings (String, BigDecimal, Boolean, etc.)  |

## Testing

### Unit Tests (`plugins/*/src/test/`)

Unit tests use the **Spock Framework** and run on JUnit Platform.

### Integration / Functional Tests (`examples/`)

The `Patient` domain object in the example app has encrypted fields of most of the supported types.
The integration tests live in `examples/sample-hibernate5`; `examples/sample-hibernate7` uses the sources of
that app (see its `build.gradle`), so add sources and tests to `sample-hibernate5` only.

## Build-Logic Convention Plugins

Convention plugins in `build-logic/src/main/groovy/` standardize build configuration:

| Plugin                 | Purpose                                                                              |
|------------------------|--------------------------------------------------------------------------------------|
| `app-run.gradle`       | Debug flags for `bootRun`                                                            |
| `compile.gradle`       | Java/Groovy compilation settings (UTF-8, incremental, Java release from `.sdkmanrc`) |
| `docs.gradle`          | Documentation aggregation (Groovydoc + Asciidoctor)                                  |
| `example-app.gradle`   | Example app config (grails-web, GSP, assets)                                         |
| `grails-assets.gradle` | Asset pipeline with Bootstrap/jQuery WebJars                                         |
| `grails-plugin.gradle` | Grails plugin application                                                            |
| `publish.gradle`       | Per-project Maven publishing metadata                                                |
| `publish-root.gradle`  | Root-level Nexus publishing workaround                                               |
| `testing.gradle`       | Test framework config (Spock, JUnit Platform, test-logger)                           |

## CI/CD

- **CI** (`.github/workflows/ci.yml`): Builds and tests on push/PR; publishes snapshots to Maven Central Snapshots on
  push to release branches.
- **Release** (`.github/workflows/release.yml`): 4-stage pipeline triggered by GitHub release — stage artifacts, release
  to Maven Central, publish docs to GitHub Pages, bump version.
- **Release Notes** (`.github/workflows/release-notes.yml`): Auto-drafts release notes using release-drafter with
  category labels.

## Code Conventions

- Groovy source files use standard Grails conventions (services and taglibs in `grails-app/`, other classes in
  `src/main/groovy/`).
- **Use `def` for local variables** where the type is inferred from the right-hand side (e.g., constructor calls,
  method calls, casts, factory methods). Explicit types should only be used for local variables when the type cannot
  be inferred or when needed for `@CompileStatic` compilation. This applies to both production code and tests.
- When writing Gradle, always use the latest best practices to avoid eager initialization.
