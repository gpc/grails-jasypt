# Grails Jasypt Encryption Plugin

Strong field-level encryption support for Grails GORM `String` fields, built on [Jasypt](http://www.jasypt.org) and [Bouncy Castle](http://www.bouncycastle.org/java.html).

## Installation

Version 6 of the plugin requires Grails 8 and Java 21. Add the flavour matching the GORM Hibernate version of your app:

```groovy
dependencies {
    // Apps using Hibernate 5 (grails-data-hibernate5)
    implementation 'io.github.gpc:grails-jasypt-encryption-hibernate5:6.0.0'

    // or apps using Hibernate 7 (grails-data-hibernate7)
    implementation 'io.github.gpc:grails-jasypt-encryption-hibernate7:6.0.0'
}
```

Up to version 5 the plugin was published as `io.github.gpc:grails-jasypt-encryption`. From version 6 that artifact is
a relocation to `grails-jasypt-encryption-hibernate5`, so existing Hibernate 5 builds keep working when bumping the
version, but please switch to the new artifact name. For Grails 7, use version 5.0.0 of
`io.github.gpc:grails-jasypt-encryption`.

## Documentation

Full documentation, including installation, configuration, and usage guides, is published at:

**https://gpc.github.io/grails-jasypt**

(Until the first stable release, this points to the `snapshot` version.)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to set up a development environment and submit changes.

## License

Released under the [Apache License, Version 2.0](LICENSE.txt).
