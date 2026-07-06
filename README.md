The Grails Jasypt Encryption plugin provides strong field-level encryption support on Grails GORM String fields.  It leverages the [Jasypt](http://www.jasypt.org) simplified encryption framework that makes working with the Java Cryptographic Extension (JCE) much easier.

It also comes with the [Bouncy Castle](http://www.bouncycastle.org/java.html) encryption provider, which gives you access to the very secure [AES](http://en.wikipedia.org/wiki/Advanced_Encryption_Standard) algorithm.  

### Installation

```
dependencies {
    implementation "org.tros:jasypt-encryption:5.0.0"
}
```
If your app is using **Grails 7** or higher, then use version **5.0.0** of this plugin.

**Requirements for Grails 7:**
- Java 17 or higher
- Gradle 8.5 or higher  

#### Previous versions  
If your app is using **Grails 4**, then use version **4.0.3** of this plugin.  

#### No longer supported
If your app is using **Grails 3** or higher, then use version **2.0.2** of this plugin.  
If your app is using **Grails 2** and **Hibernate 4**, then use version **1.3.1** of this plugin.  
If your app is using **Grails 2** and **Hibernate 3**, then use version **1.2.1** of this plugin.  

### Configuration

You'll then need to configure the encryption in your Grails configuration using a stanza like this (make sure to change the password):

```
jasypt {
    algorithm = "PBEWITHSHA256AND256BITAES-CBC-BC"
    providerName = "BC"
    password = "<your very secret passphrase>"
    keyObtentionIterations = 1000
}
```

This will configure your encryption to use a 256 bit AES algorithm to do the encryption.  Key generation will also be repeated 1000 times (to slow down any brute force attacks).   Changing any of these values will result in different results (and will also prevent the ability to decrypt previously encrypted information).

### External Config Files in Grails

You can put the encryption configuration in Config.groovy, but a better location would be an external configuration file that does not get checked into source code (and can vary based on environment).  Just put something like this in your Config.groovy file to define an external config file:

```
def configFIlePath = System.getenv('ENCRYPTION_CONFIG_LOCATION') ?: "file:${userHome}/.jasypt"
grails.config.locations = [configFilePath]
```

This enables you to set an environment variable with the location of the encryption configuration, otherwise it will look for a .encryption file in the current user's home (useful for developers).

### Enabling "Unlimited" Encryption in Java 

Because of American export standards, and the notion that [cryptographically strong algorithms are "munitions"](http://en.wikipedia.org/wiki/Export_of_cryptography_in_the_United_States), you'll want to make sure that the encryption policy jars that came with your installation of Java allow "unlimited" encryption, rather than the default "strong" (which are actually pretty weak). 

To check to see what level of encryption your installation of Java has, you'll need to do one of two things:
A:  Run the code from this [gist on github](https://gist.github.com/jehrhardt/5167854)

or

B:  crack open your `$JAVA_HOME/jre/lib/security/local_policy.jar` to see what's inside.:

```
% cd /tmp
% cp $JAVA_HOME/jre/lib/security/local_policy.jar .
% jar xvf local_policy.jar
 inflated: META-INF/MANIFEST.MF
 inflated: META-INF/JCE_RSA.SF
 inflated: META-INF/JCE_RSA.RSA
  created: META-INF/
 inflated: default_local.policy

% cat default_local.policy
// Country-specific policy file for countries with no limits on crypto strength.
grant {
    // There is no restriction to any algorithms.
    permission javax.crypto.CryptoAllPermission;
};
```

If the permissions look like that, you're set.    Mac laptops bought in the US have this configured by default, all other installations I've seen have needed to be upgraded (including all Linux distros).

Installing the new jar files is easy, just go download the "Java Cryptography Extension (JCE)" [under "Other Downloads" on Oracle's website](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

Unzip the zip file and copy the jar files into your `$JAVA_HOME/jre/lib/security directory` to overwrite the existing, limited encryption jars.  

```sh
#make a backup copy of your existing files
mkdir old_java_security
cp -r $JAVA_HOME/jre/lib/security old_java_security

unzip jce_policy-6.zip
sudo cp jce/*.jar $JAVA_HOME/jre/lib/security
```

### Defining Fields in your Domain to Encrypt

Defining fields in your domain object that you want to be encrypted within the database is easy once the plugin is installed and configured, just define the "type" within the domain class' mapping:

```
package com.bloomhealthco.domain

import com.bloomhealthco.jasypt.GormEncryptedStringType

class Member {
    String firstName
    String lastName
    String ssn
    static mapping = {
    	ssn type: GormEncryptedStringType
    }
}
```

One other caveat, if you're setting the length of the field within the database schema, you'll need to give yourself extra room as the encrypted value will be longer than the unencrypted value was.   This length will depend on the encryption algorithm that you use.  It's easy to write an integration test that can spit out all of the encrypted lengths for you.  See `testEncryptionWithLongNamesFit()` in the https://github.com/ZenHarbinger/grails-jasypt/blob/master/src/test/projects/sample/src/integration-test/groovy/com/bloomhealthco/domain/JasyptDomainEncryptionTests.groovy for an example.

### Custom Encryption Types

As of version 1.1 the plugin provides 'Gorm' versions of all the built in Encrypted types provide by the jasypt plugin, http://www.jasypt.org/hibernate.html.

It is also possible to define your own GORM encrypted types. This happens in two steps. First, you need a UserType that handles the encryption. This might be a class you have already from a existing java application or you could extend the jasypt class  [AbstractEncryptedAsStringType](https://jasypt.svn.sourceforge.net/svnroot/jasypt/trunk/jasypt-hibernate3/src/main/java/org/jasypt/hibernate3/type/AbstractEncryptedAsStringType.java). Second, you define a new Gorm Encrypted type that composes that UserType to provide the wiring to the Grails configuration.

Here's an example that can encrypt joda-time dates (requires the joda-time jar to work):

```
package com.bloomhealthco.jasypt

import org.jasypt.hibernate.type.AbstractEncryptedAsStringType
import org.joda.time.LocalDate;

public class EncryptedLocalDateAsStringType extends AbstractEncryptedAsStringType {

    protected Object convertToObject(String string) {
        if (!string) return null
        if (!(string =~ /\d{4}-\d{2}-\d{2}/)) throw new IllegalArgumentException("String does not match YYYY-MM-dd pattern")
        new LocalDate(string)
    }

    protected String convertToString(Object object) {
        if (!object) return null
        if (object.class != LocalDate) throw new IllegalArgumentException("Expected ${LocalDate.name} but was ${object.class.name}")
        object.toString("YYYY-MM-dd")
    }

    public Class returnedClass() { LocalDate }
}

public class GormEncryptedLocalDateAsStringType extends JasyptConfiguredUserType<EncryptedLocalDateAsStringType> {
}

```

### Further Documentation

For now, documentation is a little light.  There's a [test project](https://github.com/ZenHarbinger/grails-jasypt/tree/master/src/test/projects/sample) checked in as part of the repository that shows the plugin being used (and has tests that exercise the functionality).  The [Patient domain object](https://github.com/ZenHarbinger/grails-jasypt/blob/master/src/test/projects/sample/grails-app/domain/com/bloomhealthco/domain/Patient.groovy) has encrypted firstName and lastName fields on it.


### Release Notes
* 5.0.0 - Migrated to Grails 7.0.0
* 4.0.4 - Move to maven central, new group ID, can compile against Grails > 4.0.5
* 4.0.3 - initial support for Grails 4
* 2.0.1 - initial support for Grails 3
* 1.3.1 - initial support for Hibernate 4
* 1.2.1 - code cleanup contribution from Burt Beckwith
* 1.2.0 - support grails 2.3.8 and up
* 1.1.0 - Support all the encrypted UserTypes provided by jasypt's hibernate support while reusing the existing implementations; thanks to Jon Palmer for pull request & work on this
* 1.0.0 - upgraded to jasypt 1.9 - support for grails 2.0.0 thanks to pull request from Jon Palmer
* 0.1.2 - upgraded to jasypt 1.7 - support for hibernate 3.6 and grails 1.3.6.  Could also work with earlier versions of grails but it has only been tested on 1.3.6 currently
