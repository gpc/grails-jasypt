package jasypt.encryption

import grails.plugins.*
import java.security.Security

import org.bouncycastle.jce.provider.BouncyCastleProvider

class JasyptEncryptionGrailsPlugin extends Plugin {

    static {
        // Adds the BouncyCastle provider to Java so we don't need to manually modify our java install.
        // Be sure that you've installed the Java Cryptography Extension (JCE) from the Oracle website
        // so that you have "unlimited" (rather than "strong", which isn't really strong) encryption.
        // You'll need to update the jars in your $JAVA_HOME/lib/security with the updated JCE jars.
        Security.addProvider new BouncyCastleProvider()
    }

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "4.0.0 > *"
  
    def title = "Jasypt Encryption"
    def description = 'Integration with Jasypt, allows easy encryption of information including Hibernate/GORM integration'
    def license = "APACHE"
    def developers = [
            [name: "Ted Naleid", email: 'contact@naleid.com'],
            [name: "Jon Palmer"],
            [name: "Dan Tanner", email: 'dan@dantanner.com'],
            [name: "Matt Aguirre", email: 'matt@tros.org'],
            [name: "Søren Berg Glasius", email: "soeren@glasius.dk"]
    ]
    def documentation = "http://grails.org/plugin/jasypt-encryption"
    def issueManagement = [system: "GITHUB", url: "https://github.com/ZenHarbinger/grails-jasypt/issues"]
    def scm = [url: "https://github.com/ZenHarbinger/grails-jasypt"]
    def profiles = ['web']

}
