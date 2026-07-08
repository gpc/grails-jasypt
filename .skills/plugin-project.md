# Plugin Project Best Practices

## Purpose

The `plugin/` directory contains the Grails plugin artifact. It is the only publishable library in this repository. It
must contain ONLY the plugin source code and unit tests; nothing else.

## Core Rules

### Plugin project contains ONLY plugin code and unit tests

The plugin project (`plugin/`) must contain:

- Plugin source code under `src/main/groovy/` and `grails-app/`
- Unit tests under `src/test/groovy/`
- Plugin configuration files under `grails-app/conf/`

The plugin project must NOT contain:

- Integration tests (these belong in example apps under `examples/`)
- Functional tests (these belong in example apps under `examples/`)
- Example controllers, views, or domain classes
- Test controllers or test-specific artifacts
- Application-level configuration not related to the plugin (e.g., database config, asset config)

### Why: separation of concerns

Keeping integration/functional tests out of the plugin project ensures:

1. The plugin artifact is clean – no test dependencies or test code leaks into the published JAR
2. Tests that require a running Grails application exercise the plugin as a real consumer would
3. The plugin's API surface is validated from the outside, not the inside
4. Different example apps can test different configurations of the plugin

## Project Structure

```
plugin/
├── build.gradle                          # Only convention plugins + dependencies
├── grails-app/
│   ├── conf/
│   │   ├── application.yml               # Plugin-specific config defaults
│   ├── controllers/                      # Interceptors, controller-scoped artifacts
│   │   └── org/grails/plugins/exampleplugin/
│   │       └── ExamplePluginInterceptor.groovy
└── src/
    ├── main/groovy/                      # Core plugin classes
    │   └── org/grails/plugins/exampleplugin/
    │       ├── ExamplePluginAutoConfiguration.groovy
    │       ├── ExamplePluginFilter.groovy
    │       ├── ExamplePluginGrailsPlugin.groovy
    │       ├── ExamplePluginResponseWrapper.groovy
    │       ├── config/
    │       │    ├── EnabledCondition.groovy
    │       │    └── ExamplePluginConfig.groovy
    │       └── core/
    │           ├── Metric.groovy
    │           └── TimingMetric.groovy
    ├── main/resources/
    │   ├── META-INF/spring
    │   │    └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    │   └── spring-configuration-metadata.json
    └── test/groovy/                      # Unit tests ONLY
        └── org/grails/plugins/exampleplugin/
            ├── MetricSpec.groovy
            └── TimingMetricSpec.groovy
```

## build.gradle Pattern

The plugin's `build.gradle` should be minimal -- apply convention plugins and declare dependencies:

```groovy
plugins {
    id 'config.compile'
    id 'config.testing'
    id 'config.grails-plugin'
    id 'config.publish'
}

version = projectVersion
group = 'io.github.gpc'

dependencies {

    profile 'org.apache.grails.profiles:web-plugin'
    console 'org.apache.grails:grails-console'

    compileOnly platform("org.apache.grails:grails-bom:$grailsVersion")
    compileOnly 'org.apache.grails:grails-dependencies-starter-web'

    testImplementation platform("org.apache.grails:grails-bom:$grailsVersion")
    testImplementation 'org.apache.grails:grails-dependencies-starter-web'
    testImplementation 'org.apache.grails:grails-dependencies-test'
}
```

Key patterns:

- Use `compileOnly` for framework dependencies the consuming application will provide
- Use `testImplementation` for test-only dependencies
- Apply `config.publish` to configure Maven publishing metadata
- NEVER add custom task configuration here – move it to a convention plugin

## Unit Test Guidelines

Unit tests in the plugin project test individual classes in isolation:

- Test domain logic, validation, and data structures (e.g., `Metric`, `TimingMetric`)
- Test utility classes (e.g., `ServerTimingUtils`)
- Use Spock Framework with `@Unroll` for data-driven tests
- Do NOT start the Grails application context for unit tests
- Do NOT make HTTP requests in unit tests
- Do NOT test controller actions, interceptors, or filters end-to-end in the plugin project

### What belongs in unit tests

- RFC 7230 name validation on `Metric`
- Start/stop lifecycle and duration calculation
- `toHeaderValue()` output formatting
- Serialization round-trips
- Equals/hashCode contracts
- Validation error cases

### What does NOT belong in unit tests

- Testing that the `Server-Timing` header appears in HTTP responses (integration test)
- Testing that the filter/interceptor wire up correctly in a running app (integration test)
- Testing timing accuracy across controller actions (integration test)
- Testing behavior with GSP views, JSON rendering, or static assets (functional test)

## Plugin Descriptor

The `ServerTimingGrailsPlugin` class extends `grails.plugins.Plugin` and exposes important
information about the plugin to the Grails framework.

## Dependency Scoping

- **`compileOnly`**: Framework dependencies the host app provides (Grails web, servlet API)
- **`implementation`**: Dependencies the plugin bundles and needs at runtime (use sparingly)
- **`testImplementation`**: Test framework dependencies (Spock, grails-testing-support)
- **`console`**: Grails console support
- **`profile`**: The Grails profile (web-plugin for plugins)

Avoid `implementation` for Grails/Spring/Servlet dependencies -- the consuming application provides these.
