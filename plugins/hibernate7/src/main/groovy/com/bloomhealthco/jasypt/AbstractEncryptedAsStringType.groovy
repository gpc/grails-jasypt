package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

import org.hibernate.type.descriptor.WrapperOptions
import org.jasypt.encryption.pbe.PBEStringEncryptor

/**
 * Base class of the user types storing the encrypted value as a string.
 *
 * @param <T> the type of the mapped property
 */
@CompileStatic
abstract class AbstractEncryptedAsStringType<T> extends AbstractEncryptedUserType<T, PBEStringEncryptor> {

    protected abstract T convertToObject(String string)

    protected String convertToString(T value) {
        value.toString()
    }

    @Override
    protected PBEStringEncryptor createEncryptor(JasyptEncryptorParameters parameters) {
        parameters.createStringEncryptor()
    }

    @Override
    int getSqlType() {
        Types.VARCHAR
    }

    @Override
    T nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        def stringEncryptor = encryptor
        def message = rs.getString(position)
        rs.wasNull() ? null : convertToObject(stringEncryptor.decrypt(message))
    }

    @Override
    void nullSafeSet(PreparedStatement st, T value, int index, WrapperOptions options) throws SQLException {
        def stringEncryptor = encryptor
        if (value == null) {
            st.setNull(index, sqlType)
        } else {
            st.setString(index, stringEncryptor.encrypt(convertToString(value)))
        }
    }
}
