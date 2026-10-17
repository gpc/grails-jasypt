# Contributing to Grails Jasypt Encryption

Thank you for your interest in contributing! This guide will help you get started.

## Code of Conduct

This project has a Code of Conduct. By participating, you are expected to uphold it. Please report
unacceptable behavior to the project maintainers.

## Getting Started

### Prerequisites

Install [SDKMAN!](https://sdkman.io/) to manage JDK, Gradle, and Groovy versions:

```bash
curl -s "https://get.sdkman.io" | bash
```

### Setting Up the Development Environment

```bash
# Clone the repository
git clone https://github.com/gpc/grails-jasypt.git
cd grails-jasypt

# Install the required SDK versions (Java 21, Gradle 9.8.1, Groovy 5.1.3)
sdk env install

# Build the project
./gradlew build
```

### Project Structure

```
grails-jasypt/
├── plugins/core/        # The publishable Grails plugin with the shared configuration (source + unit tests ONLY)
├── plugins/hibernate5/  # The publishable Hibernate 5 encrypted types (source + unit tests ONLY)
├── plugins/hibernate7/  # The publishable Hibernate 7 encrypted types (source + unit tests ONLY)
├── plugins/relocation/  # Relocation POM for the pre-6.0 artifact name grails-jasypt-encryption
├── examples/sample-hibernate5/  # Example app with integration tests
├── examples/sample-hibernate7/  # Runs the sample-hibernate5 sources and tests on Hibernate 7
├── build-logic/         # Gradle convention plugins (shared build configuration)
├── docs/                # Asciidoctor documentation
└── .skills/             # AI agent best-practice docs
```

Key architectural rules:

- **Plugin modules** contain only plugin source code and unit tests – no integration tests, no example controllers.
- **Example apps** under `examples/` host all integration and functional tests. They depend on the plugin as a real
  consumer would.
- **Convention plugins** in `build-logic/` deduplicate build configuration. Never use `subprojects {}`,
  `allprojects {}`, or `configure()` blocks in the root `build.gradle`.

## Building and Testing

```bash
# Full build (compile + all tests)
./gradlew build

# Plugin unit tests only
./gradlew :grails-jasypt-encryption-core:test :grails-jasypt-encryption-hibernate5:test :grails-jasypt-encryption-hibernate7:test

# Integration tests (runs the example apps on Hibernate 5 and Hibernate 7)
./gradlew :sample-hibernate5:integrationTest :sample-hibernate7:integrationTest

# Run the example app locally
./gradlew :sample-hibernate5:bootRun

# Generate documentation
./gradlew docs

# Skip tests
./gradlew build -PskipTests

# Clean build
./gradlew clean build
```

### Code Coverage

The project uses JaCoCo to aggregate coverage data from both plugin unit tests and example app integration tests.

```bash
# Generate the aggregated coverage report
./gradlew jacocoAggregatedReport
```

Reports are generated at:

| Report                          | Location                                                                         |
|---------------------------------|----------------------------------------------------------------------------------|
| Aggregated (unit + integration) | `code-coverage/build/reports/jacoco/jacocoAggregatedReport/html/index.html`      |
| Plugin unit tests               | `plugins/<module>/build/reports/jacoco/test/html/index.html`                     |
| Sample app integration tests    | `examples/<app>/build/reports/jacoco/jacocoIntegrationTestReport/html/index.html` |

The aggregated report is also produced automatically as part of `./gradlew build`, so you can view it after any full
build.

## Making Changes

### Branching Strategy

- Create a feature branch from the current release branch (e.g., `6.0.x`):
    - `feature/short-description` for new features
    - `fix/short-description` for bug fixes
    - `docs/short-description` for documentation changes
    - `refactor/short-description` for refactoring

These branch prefixes are used by [release-drafter](https://github.com/release-drafter/release-drafter) to automatically
categorize changes in release notes.

### Coding Standards

- **Language:** Groovy 5.1 on Java 21
- **Framework:** Grails 8.0
- **Testing:** Spock Framework on JUnit Platform
- Follow existing code conventions in the project
- Changes to the encrypted types must be made to both the Hibernate 5 and the Hibernate 7 module, and must not
  change the format of the stored values

### Gradle Conventions

- Always use lazy APIs: `tasks.register()`, `tasks.named()`, `configureEach`, `provider {}`
- Never use eager task creation (`tasks.create()`, `project.task()`)
- If two or more subprojects share build logic, extract it into a convention plugin in `build-logic/`

## Submitting a Pull Request

1. **Ensure all tests pass** locally: `./gradlew build`
2. **Write tests** for new functionality:
    - Unit tests go in `plugins/<module>/src/test/`
    - Integration tests go in `examples/sample-hibernate5/src/integration-test/` (they also run on Hibernate 7)
3. **Update documentation** if you changed behavior or added features (in `docs/src/docs/`)
4. **Push your branch** and open a pull request against the release branch
5. **Fill out the PR template** completely

### What to Expect

- CI will run automatically on your PR
- A maintainer will review your changes
- You may be asked to make revisions
- Once approved, a maintainer will merge your PR

## Reporting Issues

- Report bugs and request enhancements in [GitHub issues](https://github.com/gpc/grails-jasypt/issues)
- Check [existing issues](https://github.com/gpc/grails-jasypt/issues) before creating a new one

## Security Vulnerabilities

If you discover a security vulnerability, **do not open a public issue**. Instead, please report it privately to the
project maintainers and include details to help reproduce and assess the issue. Responsible disclosure helps us protect
users while we investigate and prepare a fix.

## License

By contributing to this project, you agree that your contributions will be licensed under
the [Apache License 2.0](LICENSE).
