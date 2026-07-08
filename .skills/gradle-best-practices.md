# Gradle Best Practices

## Purpose

This skill covers Gradle best practices for this project, including convention plugins, extension configuration,
lazy APIs, and build structure. Convention plugins remove duplication across subprojects by centralizing shared
build logic. They live in the `build-logic/` composite build and are applied by ID in each subproject's `build.gradle`.

## Core Rules

### NEVER configure subprojects from the root build.gradle

The root `build.gradle` must NEVER use `subprojects {}`, `allprojects {}`, or `configure(subprojects.matching {...}) {}`
to apply plugins or configure subproject behavior. This is an antipattern that causes ordering issues, breaks project
isolation, and makes builds harder to reason about.

```groovy
// BAD - Never do this in root build.gradle
subprojects {
    apply plugin: 'groovy'
    dependencies {
        implementation 'org.example:shared-lib:1.0'
    }
}

// BAD - Never do this either
allprojects {
    repositories {
        mavenCentral()
    }
}
```

Instead, create a convention plugin in `build-logic/` and apply it in each subproject that needs it:

```groovy
// GOOD - build-logic/src/main/groovy/config.compile.gradle
plugins {
    id 'groovy'
}
// shared compilation config here
```

```groovy
// GOOD - plugin/build.gradle
plugins {
    id 'config.compile'
}
```

The ONLY exception is the `config.publish-root` convention plugin, which exists solely as a workaround for a Nexus
publishing bug (https://github.com/gradle-nexus/publish-plugin/issues/310) that requires version/group to be set at the
root level.

### Use the composite build pattern

Convention plugins reside in `build-logic/`, which is included as a composite build via `settings.gradle`:

```groovy
pluginManagement {
    includeBuild('./build-logic') {
        it.name = 'build-logic'
    }
}
```

### Naming convention

Convention plugin files follow the pattern:

```
build-logic/src/main/groovy/config.<purpose>.gradle
```

The plugin ID matches the filename (minus the `.gradle` extension). For example:

- `config.compile.gradle` -> plugin ID `config.compile`

### Declare external plugin dependencies in build-logic/build.gradle

When a convention plugin applies a third-party plugin, that plugin must be declared as an `implementation` dependency in
`build-logic/build.gradle`:

```groovy
// build-logic/build.gradle
plugins {
    id 'groovy-gradle-plugin'
}

dependencies {
    implementation platform("org.apache.grails:grails-bom:${gradleProperties.grailsVersion}")
    implementation 'org.apache.grails:grails-gradle-plugins'
    implementation "com.adarshr:gradle-test-logger-plugin:${gradleProperties.testLoggerVersion}"
    implementation 'cloud.wondrify:asset-pipeline-gradle'
    implementation 'org.apache.grails.gradle:grails-publish'
}
```

### Share properties from root gradle.properties

The `build-logic/build.gradle` reads the root `gradle.properties` and exposes those values as extra properties so
convention plugins can reference them (e.g., `grailsVersion`):

```groovy
file('../gradle.properties').withInputStream { is ->
    extensions.extraProperties.set(
            'gradleProperties',
            new Properties().tap { load(is) }
    )
}

allprojects { project ->
    gradleProperties.stringPropertyNames().each { key ->
        project.extensions.extraProperties.set(
                key,
                gradleProperties.getProperty(key)
        )
    }
}
```

## Avoid Eager Initialization

Always use lazy/deferred APIs to avoid eagerly resolving tasks or configurations:

```groovy
// GOOD - lazy task configuration
tasks.withType(JavaCompile).configureEach {
    options.encoding = StandardCharsets.UTF_8.name()
}

tasks.named('bootRun', JavaExec) {
    doFirst { /* ... */ }
}

tasks.register('docs') {
    dependsOn(/* ... */)
}

// BAD - eager resolution
tasks.withType(JavaCompile) { // missing .configureEach
    options.encoding = 'UTF-8'
}

task docs { // old task() API is eager
    dependsOn /* ... */
}
```

Key APIs to use:

- `tasks.register()` instead of `task()`
- `tasks.named()` instead of `tasks.getByName()`
- `tasks.withType(X).configureEach {}` instead of `tasks.withType(X) {}`
- `project.provider {}` for lazy values
- `layout.buildDirectory` instead of `buildDir`
- `dependsOn()` method instead of `dependsOn =` setter (setter replaces all dependencies; the method adds to them)
- Do NOT chain `.configure {}` on `tasks.register()` or `tasks.named()` — pass the closure directly to preserve type hints

## Extension Configuration with Type Hints

When configuring project extensions (like publishing metadata or third-party plugin configurations), use
`extensions.configure(Type)` with explicit `it` for type hints and better IDE support:

```groovy
// GOOD - explicit it in extensions.configure() for type hints
extensions.configure(GrailsPublishExtension) {
    it.artifactId = project.name
    it.githubSlug = 'gpc/grails-export'
    it.license.name = 'Apache-2.0'
    it.title = 'My Plugin'
    it.developers = [name: 'Developer Name']
}
```

Explicit `it` is NOT required in `tasks.named()`, `tasks.register()`, or `configureEach` — these already have typed
delegates:

```groovy
// GOOD - no explicit it needed, delegate is already typed
tasks.withType(Checkstyle).configureEach {
    group = 'verification'
    onlyIf { !project.hasProperty('skipCodeStyle') }
}

tasks.named('bootRun', JavaExec) {
    doFirst {
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
    }
}
```

**Benefits of `extensions.configure(Type)` with explicit `it`:**

- IDE auto-completion and type-checking for extension properties
- Clearer intent: code readers immediately see the extension type being configured
- Reduces runtime errors from typos in property names

## Composition Over Inheritance

Convention plugins should compose by applying other convention plugins rather than duplicating logic:

```groovy
// example.gradle applies other convention plugins
plugins {
    id 'org.apache.grails.gradle.grails-web'
    id 'org.apache.grails.gradle.grails-gsp'
    id 'config.grails-assets'
    id 'config.app-run'
}
```

## Existing Convention Plugins

| Plugin                           | Purpose                                                                              |
|----------------------------------|--------------------------------------------------------------------------------------|
| `app-run.gradle`                 | Debug flags for `bootRun`                                                            |
| `code-coverage.gradle`           | JaCoCo coverage for project (XML + HTML reports)                                     |
| `code-coverage-aggregate.gradle` | JaCoCo coverage aggregation across subprojects (XML + HTML reports)                  |
| `code-style.gradle`              | Checkstyle + CodeNarc code style checking (configs in `build-logic/config/`)         |
| `compile.gradle`                 | Java/Groovy compilation settings (UTF-8, incremental, Java release from `.sdkmanrc`) |
| `docs.gradle`                    | Documentation aggregation (Groovydoc + Asciidoctor)                                  |
| `example-app.gradle`             | Example app config (grails-web, GSP, assets)                                         |
| `grails-assets.gradle`           | Asset pipeline with Bootstrap/jQuery WebJars                                         |
| `grails-plugin.gradle`           | Grails plugin application                                                            |
| `publish.gradle`                 | Per-project Maven publishing metadata                                                |
| `publish-root.gradle`            | Root-level Nexus publishing workaround                                               |
| `testing.gradle`                 | Test framework config (Spock, JUnit Platform, test-logger)                           |

## When to Create a New Convention Plugin

Create a new convention plugin when:

- Two or more subprojects share the same build configuration
- A subproject's `build.gradle` grows beyond applying plugins and declaring dependencies
- You need to enforce a project-wide standard (e.g., code formatting, static analysis)

Keep each convention plugin focused on a single concern. Prefer small, composable plugins over monolithic ones.

## Repository Management

Repositories are managed centrally in `settings.gradle` via `dependencyResolutionManagement`:

```groovy
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        maven { url = 'https://repo.grails.org/grails/restricted' }
    }
}
```

This prevents subprojects from declaring their own repositories, ensuring consistency. The `FAIL_ON_PROJECT_REPOS` mode
enforces this.
