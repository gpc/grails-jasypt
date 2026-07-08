package com.bloomhealthco.jasypt

import grails.util.Holders
import groovy.transform.CompileStatic

import org.hibernate.usertype.ParameterizedType
import org.hibernate.usertype.UserType
import org.jasypt.hibernate5.type.ParameterNaming

@CompileStatic
class JasyptConfiguredUserType<T extends UserType & ParameterizedType> extends DefaultParametersUserType<T> {

    /**
     *  you can define an encryptor in your grails-app/conf/spring/resources.groovy file that
     *  contains an encryptor with a default name of 'gormEncryptor'
     *  ex:
     *  beans = {
     *     hibernateStringEncryptor(org.jasypt.hibernate.encryptor.HibernatePBEStringEncryptor) {
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
     *     hibernateStringEncryptor(org.jasypt.hibernate.encryptor.HibernatePBEStringEncryptor) {
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
    Map getDefaultParameters() {
        Map<String, Object> defaultParameters = new LinkedHashMap<String, Object>((int) jasyptConfig.size())
        defaultParameters.putAll(jasyptConfig)
        if (
        !defaultParameters[ParameterNaming.ALGORITHM] &&
                !defaultParameters[ParameterNaming.PASSWORD] &&
                !defaultParameters[ParameterNaming.KEY_OBTENTION_ITERATIONS] &&
                !defaultParameters[ParameterNaming.ENCRYPTOR_NAME]
        ) {
            defaultParameters[ParameterNaming.ENCRYPTOR_NAME] = 'gormEncryptor'
        }

        return defaultParameters
    }

    /**
     * You can create a jasypt stanza in your grails-app/conf/Config.groovy (or another config file that
     * Config.groovy pulls in).  This stanza can either override the default encryptor name and
     * set a new encryptorRegisteredName that you define in your Spring resources.groovy file, ex:
     *
     * jasypt {
     *     encryptorRegisteredName = "fooBar"
     * }
     *
     * otherwise, you can actually configure the encryptor right there using the other properties
     * available in org.jasypt.hibernate.type.ParameterNaming, ex with triple-DES:
     *
     * jasypt {
     *     algorithm = "PBEWithMD5AndTripleDES"
     *     password = "s3kr1t"
     *     keyObtentionIterations = 1000
     * }
     *
     * or Bouncy Castle AES:
     *
     * jasypt {
     *     algorithm = "PBEWITHSHA256AND128BITAES-CBC-BC"
     *     providerName = "BC"
     *     password = "s3kr1t"
     *     keyObtentionIterations = 1000
     * }
     *
     * @return the jasypt config specified in Config.groovy
     */
    Map<String, Object> getJasyptConfig() {
        (Map<String, Object>) (Holders.config?.jasypt ?: [:])
    }
}
