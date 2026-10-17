package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

import org.jasypt.encryption.pbe.PBEBigDecimalEncryptor
import org.jasypt.encryption.pbe.PBEBigIntegerEncryptor
import org.jasypt.encryption.pbe.PBEByteEncryptor
import org.jasypt.encryption.pbe.PBEStringEncryptor
import org.jasypt.encryption.pbe.StandardPBEBigDecimalEncryptor
import org.jasypt.encryption.pbe.StandardPBEBigIntegerEncryptor
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.exceptions.EncryptionInitializationException
import org.jasypt.hibernate5.encryptor.HibernatePBEEncryptorRegistry
import org.jasypt.hibernate5.type.ParameterNaming

/**
 * The encryptor settings of an encrypted user type.
 *
 * Validates and applies the parameters the same way as the jasypt-hibernate5 user types do,
 * so values encrypted with the Hibernate 5 flavour of the plugin can be decrypted by this one.
 */
@CompileStatic
class JasyptEncryptorParameters {

    final String encryptorName
    final String algorithm
    final String providerName
    final String password
    final Integer keyObtentionIterations
    final String stringOutputType

    private JasyptEncryptorParameters(Map<String, Object> values) {
        encryptorName = values.encryptorName
        algorithm = values.algorithm
        providerName = values.providerName
        password = values.password
        keyObtentionIterations = (Integer) values.keyObtentionIterations
        stringOutputType = values.stringOutputType
    }

    static JasyptEncryptorParameters from(Properties parameters) {
        def encryptorName = parameters.getProperty(ParameterNaming.ENCRYPTOR_NAME)
        def algorithm = parameters.getProperty(ParameterNaming.ALGORITHM)
        def password = parameters.getProperty(ParameterNaming.PASSWORD)
        def keyObtentionIterations = parameters.getProperty(ParameterNaming.KEY_OBTENTION_ITERATIONS)

        if (encryptorName != null) {
            if (algorithm != null || password != null || keyObtentionIterations != null) {
                throw new EncryptionInitializationException(
                        "If \"${ParameterNaming.ENCRYPTOR_NAME}\" is specified, none of " +
                                "\"${ParameterNaming.ALGORITHM}\", \"${ParameterNaming.PASSWORD}\" or " +
                                "\"${ParameterNaming.KEY_OBTENTION_ITERATIONS}\" can be specified"
                )
            }
            return new JasyptEncryptorParameters(encryptorName: encryptorName)
        }
        if (password == null) {
            throw new EncryptionInitializationException(
                    "If \"${ParameterNaming.ENCRYPTOR_NAME}\" is not specified, then " +
                            "\"${ParameterNaming.PASSWORD}\" (and optionally \"${ParameterNaming.ALGORITHM}\" and " +
                            "\"${ParameterNaming.KEY_OBTENTION_ITERATIONS}\") must be specified"
            )
        }
        new JasyptEncryptorParameters(
                algorithm: algorithm,
                providerName: parameters.getProperty(ParameterNaming.PROVIDER_NAME),
                password: password,
                keyObtentionIterations: parseInteger(ParameterNaming.KEY_OBTENTION_ITERATIONS, keyObtentionIterations),
                stringOutputType: parameters.getProperty(ParameterNaming.STRING_OUTPUT_TYPE)
        )
    }

    static Integer parseInteger(String name, String value) {
        if (value == null) {
            return null
        }
        try {
            return Integer.parseInt(value)
        } catch (NumberFormatException ignored) {
            throw new EncryptionInitializationException("Value specified for \"$name\" is not a valid integer")
        }
    }

    PBEStringEncryptor createStringEncryptor() {
        if (encryptorName != null) {
            return registered(HibernatePBEEncryptorRegistry.instance.getPBEStringEncryptor(encryptorName), 'string')
        }
        def encryptor = new StandardPBEStringEncryptor(password: password)
        if (algorithm != null) {
            encryptor.algorithm = algorithm
        }
        if (providerName != null) {
            encryptor.providerName = providerName
        }
        if (keyObtentionIterations != null) {
            encryptor.keyObtentionIterations = keyObtentionIterations
        }
        if (stringOutputType != null) {
            encryptor.stringOutputType = stringOutputType
        }
        encryptor.initialize()
        return encryptor
    }

    // Like jasypt-hibernate5, the numeric and binary encryptors do not use the provider name

    PBEBigDecimalEncryptor createBigDecimalEncryptor() {
        if (encryptorName != null) {
            return registered(HibernatePBEEncryptorRegistry.instance.getPBEBigDecimalEncryptor(encryptorName), 'big decimal')
        }
        def encryptor = new StandardPBEBigDecimalEncryptor(password: password)
        if (algorithm != null) {
            encryptor.algorithm = algorithm
        }
        if (keyObtentionIterations != null) {
            encryptor.keyObtentionIterations = keyObtentionIterations
        }
        encryptor.initialize()
        return encryptor
    }

    PBEBigIntegerEncryptor createBigIntegerEncryptor() {
        if (encryptorName != null) {
            return registered(HibernatePBEEncryptorRegistry.instance.getPBEBigIntegerEncryptor(encryptorName), 'big integer')
        }
        def encryptor = new StandardPBEBigIntegerEncryptor(password: password)
        if (algorithm != null) {
            encryptor.algorithm = algorithm
        }
        if (keyObtentionIterations != null) {
            encryptor.keyObtentionIterations = keyObtentionIterations
        }
        encryptor.initialize()
        return encryptor
    }

    PBEByteEncryptor createByteEncryptor() {
        if (encryptorName != null) {
            return registered(HibernatePBEEncryptorRegistry.instance.getPBEByteEncryptor(encryptorName), 'byte')
        }
        def encryptor = new StandardPBEByteEncryptor(password: password)
        if (algorithm != null) {
            encryptor.algorithm = algorithm
        }
        if (keyObtentionIterations != null) {
            encryptor.keyObtentionIterations = keyObtentionIterations
        }
        encryptor.initialize()
        return encryptor
    }

    private <E> E registered(E encryptor, String kind) {
        if (encryptor == null) {
            throw new EncryptionInitializationException(
                    "No $kind encryptor registered for hibernate with name \"$encryptorName\""
            )
        }
        return encryptor
    }
}
