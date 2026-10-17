package com.bloomhealthco.jasypt

import grails.util.Holders
import groovy.transform.CompileStatic

import org.jasypt.hibernate5.type.ParameterNaming

/**
 * Resolves the default parameters passed to the encrypted GORM user types.
 *
 * Shared by the Hibernate 5 and Hibernate 7 flavours of the plugin, so both are
 * configured the same way.
 */
@CompileStatic
class JasyptDefaultParameters {

    static final String DEFAULT_ENCRYPTOR_NAME = 'gormEncryptor'

    private JasyptDefaultParameters() {
    }

    /**
     *  you can define an encryptor in your grails-app/conf/spring/resources.groovy file that
     *  contains an encryptor with a default name of 'gormEncryptor'
     *  ex:
     *  beans = {
     *     hibernateStringEncryptor(org.jasypt.hibernate5.encryptor.HibernatePBEStringEncryptor) {
     *         registeredName = "gormEncryptor"
     *         algorithm = "PBEWithMD5AndTripleDES"
     *         password = "s3kr1t"
     *         keyObtentionIterations = 1000
     *     }
     *  }
     *
     *  or bouncy castle AES:
     *
     *  beans = {
     *     hibernateStringEncryptor(org.jasypt.hibernate5.encryptor.HibernatePBEStringEncryptor) {
     *         registeredName = "gormEncryptor"
     *         providerName = "BC"
     *         algorithm = "PBEWITHSHA256AND128BITAES-CBC-BC"
     *         password = "s3kr1t"
     *         keyObtentionIterations = 1000
     *     }
     *  }
     *
     * @return a default config that expects an encryptor name of gormEncryptor
     */
    static Map<String, Object> getDefaultParameters() {
        defaultParameters(jasyptConfig)
    }

    /**
     * @param jasyptConfig the configured jasypt parameters
     * @return the given parameters, with the encryptor name defaulting to gormEncryptor
     *         when no encryptor is configured
     */
    static Map<String, Object> defaultParameters(Map<String, Object> jasyptConfig) {
        def defaultParameters = new LinkedHashMap<String, Object>(jasyptConfig ?: [:])
        if (
        !defaultParameters[ParameterNaming.ALGORITHM] &&
                !defaultParameters[ParameterNaming.PASSWORD] &&
                !defaultParameters[ParameterNaming.KEY_OBTENTION_ITERATIONS] &&
                !defaultParameters[ParameterNaming.ENCRYPTOR_NAME]
        ) {
            defaultParameters[ParameterNaming.ENCRYPTOR_NAME] = DEFAULT_ENCRYPTOR_NAME
        }
        return defaultParameters
    }

    /**
     * Merges the default parameters with the parameters given in the domain mapping.
     * Parameters from the mapping take precedence.
     *
     * Values are stored as is. As {@link Properties#getProperty(String)} only returns String values,
     * non-String config values (e.g. a numeric keyObtentionIterations from YAML) are ignored by the
     * user types. This has always been the behaviour of the plugin, and changing it would change the
     * encryptor settings and break decryption of existing data.
     *
     * @param parameters the parameters given to the user type, may be null
     * @return the merged parameters
     */
    static Properties mergeWithDefaults(Properties parameters) {
        def merged = new Properties()
        merged.putAll(defaultParameters)
        if (parameters) {
            merged.putAll(parameters)
        }
        return merged
    }

    /**
     * You can create a jasypt stanza in your grails-app/conf/application.yml (or application.groovy).
     * This stanza can either override the default encryptor name and set a new encryptorRegisteredName
     * that you define in your Spring resources.groovy file, ex:
     *
     * jasypt:
     *     encryptorRegisteredName: fooBar
     *
     * otherwise, you can actually configure the encryptor right there using the other properties
     * available in org.jasypt.hibernate5.type.ParameterNaming, ex with triple-DES:
     *
     * jasypt:
     *     algorithm: PBEWithMD5AndTripleDES
     *     password: s3kr1t
     *     keyObtentionIterations: 1000
     *
     * or Bouncy Castle AES:
     *
     * jasypt:
     *     algorithm: PBEWITHSHA256AND128BITAES-CBC-BC
     *     providerName: BC
     *     password: s3kr1t
     *     keyObtentionIterations: 1000
     *
     * @return the jasypt config specified in the application configuration
     */
    static Map<String, Object> getJasyptConfig() {
        (Map<String, Object>) (Holders.config?.jasypt ?: [:])
    }
}
