# Example Application Best Practices

## Purpose

Example applications under `examples/` serve as both integration/functional test harnesses and usage demonstrations for
the plugin. They are real Grails applications that depend on the plugin project, exercising it exactly as an end user
would.

## Core Rules

### All integration and functional tests belong in example apps

The plugin project (`plugin/`) must contain ONLY unit tests. Any test that requires:

- A running Grails application context
- HTTP requests and responses
- Controller/interceptor/filter behavior in a live server
- View rendering (GSP, JSON, text)
- Static asset serving
- Database interaction
- Multi-component interaction

...belongs in an example app under `examples/`.

### Example apps depend on the plugin as a project dependency

Each example app declares the plugin as a regular dependency, just as an external consumer would:

```groovy
dependencies {
    implementation project(':grails-plugin-template')
}
```

This ensures the plugin is tested through its public API and packaging, not its internals.

### Multiple example apps can test different scenarios

The `examples/` directory can contain more than one app. Different apps can test:

- Different Grails configurations
- Different database backends
- Different view technologies
- Edge cases or unusual setups
- Performance scenarios

All apps under `examples/` are auto-discovered by `settings.gradle`:

```groovy
def examples = file('examples').listFiles({ it.directory } as FileFilter)
examples.each { example ->
    include example.name
    project(":$example.name").projectDir = file("examples/$example.name")
}
```

New apps are also automatically included in coverage aggregation -- `code-coverage/build.gradle` discovers all example apps
under `examples/` at configuration time, so no manual registration is needed.

## Project Structure

```
examples/app1/
├── build.gradle
├── grails-app/
│   ├── conf/
│   │   ├── application.yml           # App config (enables plugin, DB, etc.)
│   │   ├── logback-spring.xml
│   │   └── spring/resources.groovy
│   ├── controllers/app1/
│   │   ├── ServerTimingTestController.groovy   # Controllers that exercise the plugin
│   │   └── UrlMappings.groovy
│   ├── init/app1/
│   │   ├── Application.groovy
│   │   └── BootStrap.groovy
│   ├── views/
│   │   ├── serverTimingTest/          # GSP views for testing view timing
│   │   ├── layouts/main.gsp
│   │   └── ...
│   ├── assets/                        # Static assets for testing asset timing
│   └── i18n/                          # Message bundles
└── src/
    └── integration-test/groovy/app1/
        └── ServerTimingIntegrationSpec.groovy   # Integration tests
```

## build.gradle Pattern

Example apps apply convention plugins and declare their own dependencies:

```groovy
plugins {
    id 'config.example-app'
}

version = projectVersion
group = 'app1'

dependencies {
    // The plugin under test
    implementation project(':grails-server-timing')

    // Standard Grails app dependencies
    implementation platform("org.apache.grails:grails-bom:$grailsVersion")
    implementation 'org.apache.grails:grails-core'
    implementation 'org.apache.grails:grails-web-boot'
    // ... other standard dependencies

    // Integration test dependencies
    integrationTestImplementation testFixtures('org.apache.grails:grails-geb')

    // Unit test dependencies (for any app-level unit tests)
    testImplementation 'org.apache.grails:grails-testing-support-web'
    testImplementation 'org.spockframework:spock-core'
}
```

Key patterns:

- Apply `example-app` convention plugin
- Depend on the plugin via `project(':grails-server-timing')`
- NEVER apply `project-publish` -- example apps are not published
- NEVER apply `plugin` -- example apps are applications, not plugins

## Integration Test Guidelines

Integration tests run against a live embedded Grails server using the `@Integration` annotation:

```groovy
@Integration
class ServerTimingIntegrationSpec extends Specification {

    @Shared
    RestTemplate restTemplate = new RestTemplate()

    private String getBaseUrl() {
        "http://localhost:${serverPort}"
    }

    private ResponseEntity<String> doGet(String path) {
        restTemplate.exchange("${baseUrl}${path}", HttpMethod.GET, null, String)
    }

    void "fast action should include Server Timing header"() {
        when:
        def response = doGet('/serverTimingTest/fast')

        then:
        response.headers.getFirst('Server-Timing') != null
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming.contains('action')
        serverTiming.contains('view')
    }
}
```

### What to test in integration tests

- HTTP headers are present and correctly formatted
- Timing values are within expected ranges (e.g., slow action >= 200 ms)
- Different response types (GSP views, JSON, plain text) all include headers
- Static assets include `other`/`total` metrics but not `action`/`view`
- Header format matches the W3C Server Timing specification
- Plugin behavior under different controller patterns (fast, slow, variable delay)
- Multiple operations accumulate timing correctly

### Integration test patterns

1. **Use `RestTemplate` or similar HTTP client** -- test real HTTP round-trips
2. **Verify headers, not internals** – assert on `Server-Timing` header values, not internal class state
3. **Use timing thresholds, not exact values** -- assert `>= 200ms`, never `== 203ms`
4. **Test edge cases** – static assets, JSON responses, redirects, errors
5. **Extract helper methods** – centralize header parsing (e.g., `extractDuration()`)

### Test organization

- Place integration tests under `src/integration-test/groovy/<package>/`
- Name test classes with `*IntegrationSpec` or `*FunctionalSpec` suffix
- Group related tests in a single spec class when they share setup
- Use `@Shared` for expensive objects like `RestTemplate`

## Test Controllers and Views

Example apps should include purpose-built controllers and views that exercise the plugin's features:

- **Fast actions** – verify baseline header presence
- **Slow actions** (with `Thread.sleep()`) – verify timing accuracy
- **Variable delay actions** – parameterized timing tests
- **Slow views** (GSP with embedded sleep) – verify view timing separation
- **JSON/text responses** – verify non-GSP response types
- **Multiple operations** – verify timing accumulation

These are test fixtures that live in the example app, NOT in the plugin project.

## Running Tests

```bash
# Run integration tests for app1
./gradlew :app1:integrationTest

# Run all tests (unit + integration) across all projects
./gradlew build

# Run the example app interactively
./gradlew :app1:bootRun
```
