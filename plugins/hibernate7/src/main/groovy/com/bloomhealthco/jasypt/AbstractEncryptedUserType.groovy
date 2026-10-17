package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

import org.hibernate.usertype.ParameterizedType
import org.hibernate.usertype.UserType

/**
 * Base class of the Hibernate 7 encrypted user types.
 *
 * The parameters given to the user type are merged with the default parameters from the
 * application config (see {@link JasyptDefaultParameters}). The encryptor is created lazily,
 * as encryptors registered by name are registered by Spring after the user types are created.
 *
 * @param <T> the type of the mapped property
 * @param <E> the type of the jasypt encryptor
 */
@CompileStatic
abstract class AbstractEncryptedUserType<T, E> implements UserType<T>, ParameterizedType {

    private JasyptEncryptorParameters encryptorParameters
    private E encryptor

    /**
     * Creates the encryptor used for encrypting and decrypting the values.
     */
    protected abstract E createEncryptor(JasyptEncryptorParameters parameters)

    /**
     * Hook for user types that take additional parameters.
     */
    protected void configure(Properties parameters) {
    }

    @Override
    synchronized void setParameterValues(Properties parameters) {
        def merged = JasyptDefaultParameters.mergeWithDefaults(parameters)
        encryptorParameters = JasyptEncryptorParameters.from(merged)
        encryptor = null
        configure(merged)
    }

    protected synchronized E getEncryptor() {
        if (encryptor == null) {
            if (encryptorParameters == null) {
                setParameterValues(null)
            }
            encryptor = createEncryptor(encryptorParameters)
        }
        return encryptor
    }

    @Override
    boolean equals(T x, T y) {
        Objects.equals(x, y)
    }

    @Override
    int hashCode(T x) {
        x.hashCode()
    }

    @Override
    T deepCopy(T value) {
        value
    }

    @Override
    boolean isMutable() {
        false
    }

    @Override
    Serializable disassemble(T value) {
        (Serializable) deepCopy(value)
    }

    @Override
    @SuppressWarnings('unchecked')
    T assemble(Serializable cached, Object owner) {
        deepCopy((T) cached)
    }

    @Override
    T replace(T original, T target, Object owner) {
        original
    }
}
