package jasypt.encryption

import grails.plugins.Plugin
import java.security.Security

import org.bouncycastle.jce.provider.BouncyCastleProvider

class JasyptEncryptionGrailsPlugin extends Plugin {

    static {
        // Adds the BouncyCastle provider to Java so we don't need to manually modify our java install.
        Security.addProvider new BouncyCastleProvider()
    }

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = '8.0.0 > *'

    def title = 'Jasypt Encryption'
    def description = 'Integration with Jasypt, allows easy encryption of information including Hibernate/GORM integration'
    def license = 'APACHE'
    def developers = [
            [name: 'Ted Naleid', email: 'contact@naleid.com'],
            [name: 'Jon Palmer'],
            [name: 'Dan Tanner', email: 'dan@dantanner.com'],
            [name: 'Matt Aguirre', email: 'matt@tros.org'],
            [name: 'Søren Berg Glasius', email: 'soeren@glasius.dk']
    ]
    def documentation = 'https://gpc.github.io/grails-jasypt'
    def issueManagement = [system: 'GITHUB', url: 'https://github.com/gpc/grails-jasypt/issues']
    def scm = [url: 'https://github.com/gpc/grails-jasypt']
    def profiles = ['web']

}
