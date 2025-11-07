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
    def grailsVersion = "7.0.1 > *"

    def title = "Jasypt Encryption"
    def description = 'Integration with Jasypt, allows easy encryption of information including Hibernate/GORM integration'
    def license = "APACHE"
    def developers = [
            [name: "Ted Naleid", email: 'contact@naleid.com'],
            [name: "Jon Palmer"],
            [name: "Dan Tanner", email: 'dan@dantanner.com'],
            [name: "Matt Aguirre", email: 'matt@tros.org'],
    ]
    def documentation = "http://grails.org/plugin/jasypt-encryption"
    def issueManagement = [system: "GITHUB", url: "https://github.com/ZenHarbinger/grails-jasypt/issues"]
    def scm = [url: "https://github.com/ZenHarbinger/grails-jasypt"]
    def profiles = ['web']

       Closure doWithSpring() { {->
               // TODO Implement runtime spring config (optional)
           }
       }

       void doWithDynamicMethods() {
           // TODO Implement registering dynamic methods to classes (optional)
       }

       void doWithApplicationContext() {
           // TODO Implement post initialization spring config (optional)
       }

       void onChange(Map<String, Object> event) {
           // TODO Implement code that is executed when any artefact that this plugin is
           // watching is modified and reloaded. The event contains: event.source,
           // event.application, event.manager, event.ctx, and event.plugin.
       }

       void onConfigChange(Map<String, Object> event) {
           // TODO Implement code that is executed when the project configuration changes.
           // The event is the same as for 'onChange'.
       }

       void onShutdown(Map<String, Object> event) {
           // TODO Implement code that is executed when the application shuts down (optional)
       }
}