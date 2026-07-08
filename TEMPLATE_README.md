# Using the Grails Plugin Template

This document explains how to use `grails-plugin-template` either as a starting point for a **new** Grails plugin or as a **migration target** for an existing plugin that needs a uniform build and release process.

---

## What the Template Provides

Once a plugin is in the template format it gets:

- A consistent multi-project Gradle build (`plugin/`, `examples/`, `docs/`)
- Convention plugins in `build-logic/` that handle compilation, testing, publishing, docs, and asset pipeline
- GitHub Actions for CI, snapshot publishing, multi-stage releases to Maven Central, release notes, contributor tracking, and version index updates
- Automated file sync: when a new template release is published, a PR is opened in every registered plugin repo to keep all infrastructure files up to date

---

## Starting a New Plugin

1. **Use this repo as a template** — click *Use this template* on GitHub, or clone and remove the `.git` directory.

2. **Rename the plugin subproject** in `settings.gradle`:
   ```groovy
   rootProject.name = 'my-plugin-root'

   include('plugin')
   project(':plugin').name = 'grails-my-plugin'   // artifact ID
   include('docs')
   project(':docs').name = 'grails-my-plugin-docs'
   ```

3. **Update `gradle.properties`** — see the [reference below](#gradleproperties).

4. **Update `project.yml`** — see the [reference below](#projectyml).

5. **Update `plugin/build.gradle`**:
   - Set `group` to match your Maven publication group (e.g. `io.github.gpc`).
   - Adjust `dependencies` for your plugin's requirements.

6. **Replace the plugin descriptor** at `plugin/src/main/groovy/grails/plugins/template/PluginTemplateGrailsPlugin.groovy` with your own `*GrailsPlugin.groovy`.

7. **Register the plugin** in `.github/projects.yml` so it receives future template syncs — see [Registering for Sync](#registering-for-sync).

8. **Install SDK versions**: run `sdk env install` (requires [SDKMAN](https://sdkman.io)) to get the correct Java, Gradle, and Groovy versions from `.sdkmanrc`.

---

## Migrating an Existing Plugin

The goal is to replace the plugin's ad-hoc build scripts and workflows with the template structure while keeping all plugin source code in place.

### Step 1 — Copy infrastructure files

Copy these files and directories wholesale from the template into your plugin repo. Do **not** edit them in your own repo; they are managed by the template sync:

```
.github/workflows/
.github/scripts/
.github/dependabot.yml
.github/renovate.json
.github/release-drafter.yml
.github/dependency-graph/
.skills/
build-logic/
gradle/
.editorconfig
.sdkmanrc
CONTRIBUTING.md
gradlew
gradlew.bat
LICENSE.txt
```

### Step 2 — Adopt the multi-project layout

The template expects this structure:

```
my-plugin/
├── plugin/          # Plugin source code and unit tests
│   ├── grails-app/
│   └── src/
├── examples/
│   └── app1/        # Integration / functional tests
├── docs/            # Asciidoctor documentation
├── build-logic/     # (copied from template — do not edit)
├── gradle/          # (copied from template — do not edit)
├── build.gradle
├── settings.gradle
├── gradle.properties
└── project.yml
```

Move your plugin source into `plugin/` if it isn't already there.

### Step 3 — Update `settings.gradle`

Replace your existing `settings.gradle` with the template version and change the project names:

```groovy
rootProject.name = 'grails-my-plugin-root'

include('plugin')
project(':plugin').name = 'grails-my-plugin'
include('docs')
project(':docs').name = 'grails-my-plugin-docs'
```

The `examples/` directory is auto-discovered — add a `build.gradle` there for each example app.

### Step 4 — Update `gradle.properties`

See the [reference below](#gradleproperties).

### Step 5 — Create `project.yml`

See the [reference below](#projectyml). Copy the template's `project.yml` and fill in your plugin's details.

### Step 6 — Update `plugin/build.gradle`

Apply the convention plugins and set your group and dependencies:

```groovy
plugins {
    id 'config.compile'
    id 'config.grails-plugin'
    id 'config.project-metadata'
    id 'config.publish'
    id 'config.testing'
}

version = projectVersion
group = "io.github.gpc"   // your Maven group

dependencies {
    profile 'org.apache.grails.profiles:web-plugin'
    compileOnly platform("org.apache.grails:grails-bom:$grailsVersion")
    compileOnly 'org.apache.grails:grails-dependencies-starter-web'
    testImplementation platform("org.apache.grails:grails-bom:$grailsVersion")
    testImplementation 'org.apache.grails:grails-dependencies-starter-web'
    testImplementation 'org.apache.grails:grails-dependencies-test'
}

extensions.configure(org.apache.grails.gradle.publish.GrailsPublishExtension) {
    it.githubSlug = "${githubOrg}/${githubProject}"
    it.license.name = projectLicense
    it.title = projectTitle
    it.desc = projectDescription
    it.organization {
        it.name = projectOrg
        it.url = "https://github.com/${githubOrg}"
    }
    it.setDevelopers(projectContributors as Map)
}
```

### Step 7 — Register for sync

Add the plugin to `.github/projects.yml` in **this template repo** — see [Registering for Sync](#registering-for-sync).

---

## `project.yml`

This file is the single source of metadata for the plugin. It drives documentation generation, Maven publishing metadata, release notes, and the contributor list.

```yaml
---
project:
  name: "grails-my-plugin"           # artifact ID, matches settings.gradle
  title: "Grails My Plugin"          # human-readable display name
  description: "One-line description of what the plugin does"
  org: "Grails Plugins"              # organisation display name (for Maven POM)

github:
  org: "gpc"                         # GitHub org slug (gpc or grails-plugins)
  project: "grails-my-plugin"        # GitHub repo name

license:
  name: "Apache-2.0"                 # SPDX licence identifier

contributors:                        # updated automatically by update-contributors workflow
  githubLogin: "Display Name"

versions:
  current: "1.0.0"                   # updated automatically on each release
  previous:
    - "1.0.0"
  ignore:                            # version strings to exclude from the docs index
    - "1.0.x"
    - "snapshot"
```

The `contributors` and `versions` blocks are maintained automatically by GitHub Actions — you do not need to edit them by hand after the initial setup.

---

## `gradle.properties`

| Property | Description |
|---|---|
| `projectVersion` | Current version, e.g. `1.0.0-SNAPSHOT`. Use `-SNAPSHOT` suffix on development branches. |
| `grailsVersion` | Grails BOM version to compile and test against, e.g. `7.0.11`. |
| `projectsToPublish` | Comma-separated list of subproject names to include in Maven publishing. Usually just the plugin artifact name. |
| `asciidoctorVersion` | Version of the Asciidoctor Gradle plugin used by the `docs` subproject. |
| `testLoggerVersion` | Version of the Gradle test-logger plugin. |
| `ciBuildScanPublish` | Set to `true` to publish Gradle build scans from CI. |
| `ciBuildScanTermsOfUseUrl` | Build scan terms URL — leave as-is. |
| `ciBuildScanTermsOfUseAgree` | Set to `yes` to agree to build scan terms. |

The `org.gradle.*` properties at the bottom control daemon, caching, and JVM settings — leave them unchanged unless you have a specific reason.

**Minimal example for a new plugin:**

```properties
projectVersion=1.0.0-SNAPSHOT
grailsVersion=7.0.11
projectsToPublish=grails-my-plugin
asciidoctorVersion=4.0.5
testLoggerVersion=4.0.0
ciBuildScanPublish=true
ciBuildScanTermsOfUseUrl=https://gradle.com/terms-of-service
ciBuildScanTermsOfUseAgree=yes

org.gradle.caching=true
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.jvmargs=-Dfile.encoding=UTF-8 -Xmx1024M
```

---

## Registering for Sync

To receive automated infrastructure updates, add your plugin to `.github/projects.yml` in **this template repo** (`grails-plugin-template`):

```yaml
projects:
  gpc:
    - my-plugin          # → github.com/gpc/grails-my-plugin
  grails-plugins:
    - another-plugin     # → github.com/grails-plugins/grails-another-plugin
```

Once registered, every new release of `grails-plugin-template` will open a PR in your plugin repo updating the infrastructure files. The PR is opened on a branch named `sync-files-from-template` and will auto-merge if CI passes.

### Locking a file

If you need to diverge from the template for a specific file, create a `.lock` file next to it:

```
touch .github/workflows/ci.yml.lock
```

The sync will skip any file that has a corresponding `.lock` file alongside it. Remove the lock file when you want to accept template updates again.

---

## SDK Versions

Tool versions are pinned in `.sdkmanrc`. Install [SDKMAN](https://sdkman.io) and run:

```bash
sdk env install
```

| Tool | Version |
|---|---|
| Java | `17.0.18-librca` (Liberica JDK) |
| Gradle | `8.14.4` |
| Groovy | `4.0.30` |
