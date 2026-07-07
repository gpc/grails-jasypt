# AGENTS.md - grails-plugin-template

## Project Overview

This is a **Grails Plugin Template**, that can be used either as a starting point for a new
grails project, or as base for existing plugins that needs a uniform release process

- **Language:** Groovy 4.0.30 on Java 17
- **Framework:** Grails 7.x
- **Build System:** Gradle 8.14.4 (with wrapper)
- **Current Version:** 1.0.0-SNAPSHOT
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
2. **The plugin project contains ONLY plugin code and unit tests.** No integration tests, no functional tests, no
   example controllers or views.
3. **Example apps under `examples/` host all integration and functional tests.** They depend on the plugin via
   `implementation project(':grails-plugin-template')` and test it as a real consumer would.
4. **Use Gradle convention plugins to deduplicate.** If two or more subprojects share build logic, extract it into a
   convention plugin in `build-logic/`.
5. **Always use lazy Gradle APIs** to avoid eager initialization (`tasks.register()`, `tasks.named()`, `configureEach`,
   `provider {}`).

## Repository Structure

```
grails-plugin-template/
├── .skills/             # Best practice skill files
├── plugin/              # Core Grails plugin (artifact: grails-plugin-template)
│   ├── grails-app/      #   Plugin services, domain, controller, taglibs and conf
│   └── src/main/        #   Plugin source code 
├── examples/app1/       # Example Grails app
│   └── grails-app/      #   Controllers and conf for integration testing
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

# Run only unit tests (plugin module)
./gradlew :grails-plugin-template:test

# Run integration tests (example app)
./gradlew :app1:integrationTest

# Skip tests
./gradlew build -PskipTests

# Run the example app
./gradlew :app1:bootRun

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

- Java: `17.0.18-librca`
- Gradle: `8.14.4`
- Groovy: `4.0.30`

Run `sdk env install` to set up the environment.

## Architecture

The plugin provides a grails-plugin-template mechanism:

1. **`PluginTemplateGrailsPlugin`** registers the plugin 

### Core Classes

| Class / Interface            | Location                                          | Purpose            |
|------------------------------|---------------------------------------------------|--------------------|
| `PluginTemplateGrailsPlugin` | `plugin/src/main/groovy/grails/plugins/template/` | Plugin descriptor; |

## Configuration

## Testing

There is no testset in the plugin template project.

### Unit Tests (`plugin/src/test/`)

Unit tests use the **Spock Framework** and run on JUnit Platform. 

### Integration / Functional Tests (`examples/app1/`)

The `TestController` in the example app is there for pure example. Integration and
functional tests added here depend on the plugin as a real consumer would.

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
