# Grails Plugin Repository Structure

## Purpose

This document defines the canonical structure for Grails plugin repositories. The structure enforces separation of
concerns: the plugin project contains only library code and unit tests, example apps provide integration/functional test
coverage, and build logic is centralized in convention plugins.

## Directory Layout

```
export/
├── .github/                    # CI/CD workflows and GitHub config
│   ├── workflows/
│   │   ├── ci.yml              # Build, test, publish snapshots
│   │   ├── code-coverage.yml   # Create a code coverage report
│   │   ├── code-style.yml      # Check code style
│   │   ├── release.yml         # Multi-stage release pipeline
│   │   └── release-notes.yml   # Automated release draft notes
│   ├── release-drafter.yml     # Release drafter categories/labels
│   └── dependency-graph/
│       └── external-references.yml  # Maven Central package association
│
├── build-logic/                # Gradle convention plugins (composite build)
│   ├── build.gradle            # Plugin dependencies (groovy-gradle-plugin)
│   ├── settings.gradle         # Build-logic project settings
│   ├── config/                 # Shared code style config files
│   │   ├── checkstyle/         #   Checkstyle XML configs
│   │   └── codenarc/           #   CodeNarc ruleset
│   └── src/main/groovy/        # Convention plugin files (*.gradle)
│       ├── config.app-run.gradle
│       ├── config.code-coverage.gradle
│       ├── config.code-coverage-aggregate.gradle
│       ├── config.code-style.gradle
│       ├── config.compile.gradle
│       ├── config.docs.gradle
│       ├── config.example-app.gradle
│       ├── config.grails-assets.gradle
│       ├── config.grails-plugin.gradle
│       ├── config.publish.gradle
│       ├── config.publish-root.gradle
│       └── config.testing.gradle
│
├── plugin/                     # The Grails plugin artifact
│   ├── build.gradle            # Convention plugins + dependencies only
│   ├── grails-app/
│   │   ├── conf/               # Plugin config (application.yml, logback)
│   │   └── controllers/        # Interceptors and controller artifacts
│   └── src/
│       ├── main/groovy/        # Plugin source code
│       └── test/groovy/        # Unit tests ONLY
│
├── examples/                   # Example apps (auto-discovered)
│   ├── app1/                   # first example app with the plugin enabled
│   │   ├── build.gradle
│   │   ├── grails-app/         # Standard Grails app structure
│   │   │   ├── conf/
│   │   │   ├── controllers/    # Test controllers
│   │   │   ├── views/          # Test views (GSP)
│   │   │   ├── init/
│   │   │   ├── assets/
│   │   │   └── i18n/
│   │   └── src/
│   │       └── integration-test/  # Integration & functional tests
│   └── app2/                   # second app showing disable feature
│       ├── build.gradle
│       ├── grails-app/         # Standard Grails app structure
│       │   ├── conf/
│       │   ├── controllers/    # Test controllers
│       │   ├── views/          # Test views (GSP)
│       │   ├── init/
│       │   ├── assets/
│       │   └── i18n/
│       └── src/
│           └── integration-test/  # Integration & functional tests
│
├── code-coverage/              # JaCoCo coverage aggregation
│   └── build.gradle            # Declares which projects contribute coverage data
│
├── docs/                       # Asciidoctor documentation
│   ├── build.gradle
│   └── src/docs/               # .adoc source files
│
├── build.gradle                # Root build (docs + root-publish ONLY)
├── settings.gradle             # Multi-project settings + composite build
├── gradle.properties           # Shared version properties
├── .sdkmanrc                   # SDK versions (Java, Gradle, Groovy)
├── AGENTS.md                   # AI agent instructions
├── .skills/                    # Best practice skill files
├── LICENSE                     # Apache 2.0
└── README.md
```

## Key Architectural Rules

### 1. Root build.gradle is minimal

The root `build.gradle` applies only root-level convention plugins (docs aggregation, root-publish workaround). It must
NEVER use `subprojects {}`, `allprojects {}`, or any mechanism to configure child projects. All shared configuration
flows through convention plugins.

```groovy
// Root build.gradle -- this is all that should be here
plugins {
    id 'idea'
    id 'config.docs'
    id 'config.publish-root'
}
```

### 2. Plugin project = library code + unit tests

The `plugin/` project is the published artifact. It contains:

- Source code (`src/main/groovy/`, `grails-app/`)
- Unit tests (`src/test/groovy/`)

It does NOT contain integration tests, functional tests, example controllers, or test views.

### 3. Example apps = integration/functional tests

All tests requiring a running Grails application live in example apps under `examples/`. Each app:

- Depends on the plugin via `implementation project(':grails-export')`
- Contains test controllers and views that exercise the plugin
- Contains integration tests under `src/integration-test/`
- Is auto-discovered by `settings.gradle`

### 4. Build logic is centralized

Convention plugins in `build-logic/` eliminate all duplication:

- Compilation settings: `config.compile.gradle`
- Test configuration: `config.testing.gradle`
- Plugin setup: `config.grails-plugin.gradle`
- Example app setup: `config.example-app.gradle`
- Publishing: `config.publish.gradle`
- Coverage aggregation: `config.coverage-aggregate.gradle`
- Code style checking: `config.code-style.gradle`

### 5. Centralized dependency resolution

Repositories are declared once in `settings.gradle` using `dependencyResolutionManagement`. The `FAIL_ON_PROJECT_REPOS`
mode prevents subprojects from declaring their own repositories.

### 6. Shared properties via gradle.properties

Version numbers and shared settings live in `gradle.properties` at the root:

```properties
projectVersion=0.0.1-SNAPSHOT
grailsVersion=7.0.7
```

These are available in all subprojects as project properties (`projectVersion`, `grailsVersion`).

## Adding a New Example App

1. Create a new directory under `examples/` (e.g., `examples/app2/`)
2. Add a `build.gradle` applying the convention plugins:
   ```groovy
   plugins {
       id 'config.example-app'
   }
   ```
3. Add standard Grails app structure under `grails-app/`
4. Add integration tests under `src/integration-test/groovy/`
5. The app will be auto-discovered by `settings.gradle` and automatically included in coverage aggregation -- no manual
   registration needed

## Adding a New Convention Plugin

1. Create a new file: `build-logic/src/main/groovy/config.<name>.gradle`
2. If the plugin applies third-party plugins, add their dependencies to `build-logic/build.gradle`
3. Apply the new plugin ID in the relevant subproject(s)
4. Keep the plugin focused on a single concern

## Build Commands

```bash
# Full build (all subprojects)
./gradlew build

# Plugin unit tests only
./gradlew :grails-plugin-template:test

# Example app integration tests
./gradlew :app1:integrationTest

# Aggregated coverage report (unit + integration)
./gradlew jacocoAggregatedReport

# Run an example app
./gradlew :app1:bootRun

# Generate documentation
./gradlew docs

# Clean everything
./gradlew clean

# Skip tests
./gradlew build -PskipTests
```

## SDK Management

The `.sdkmanrc` file pins exact SDK versions. Run `sdk env install` to install them. CI reads `.sdkmanrc` to determine
the Java version dynamically.

## CI/CD Pipeline

- **CI**: Builds and tests on every push/PR. Publishes snapshots on push to release branches.
- **Coverage**: Runs the full build and posts an aggregated JaCoCo coverage summary to the GitHub Actions job summary.
- **Release**: 4-stage pipeline (stage -> release -> docs -> version bump) triggered by GitHub release.
- **Release Notes**: Auto-drafts release notes from PRs using release-drafter with category labels.
