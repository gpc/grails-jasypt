package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

import org.hibernate.type.descriptor.WrapperOptions
import org.jasypt.encryption.pbe.PBEByteEncryptor

/**
 * Stores the encrypted value as binary.
 */
@CompileStatic
class GormEncryptedBinaryType extends AbstractEncryptedUserType<byte[], PBEByteEncryptor> {

    @Override
    Class<byte[]> returnedClass() {
        byte[]
    }

    @Override
    int getSqlType() {
        Types.VARBINARY
    }

    @Override
    protected PBEByteEncryptor createEncryptor(JasyptEncryptorParameters parameters) {
        parameters.createByteEncryptor()
    }

    @Override
    byte[] nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        def byteEncryptor = encryptor
        def encryptedValue = rs.getBytes(position)
        rs.wasNull() ? null : byteEncryptor.decrypt(encryptedValue)
    }

    @Override
    void nullSafeSet(PreparedStatement st, byte[] value, int index, WrapperOptions options) throws SQLException {
        def byteEncryptor = encryptor
        if (value == null) {
            st.setNull(index, sqlType)
        } else {
            st.setBytes(index, byteEncryptor.encrypt(value))
        }
    }

    @Override
    boolean equals(byte[] x, byte[] y) {
        Arrays.equals(x, y)
    }

    @Override
    int hashCode(byte[] x) {
        Arrays.hashCode(x)
    }

    @Override
    byte[] deepCopy(byte[] value) {
        value?.clone()
    }

    @Override
    boolean isMutable() {
        true
    }
}
