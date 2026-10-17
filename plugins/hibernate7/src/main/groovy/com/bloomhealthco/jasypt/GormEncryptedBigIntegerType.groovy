package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

import java.math.RoundingMode
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

import org.hibernate.type.descriptor.WrapperOptions
import org.jasypt.encryption.pbe.PBEBigIntegerEncryptor

/**
 * Stores the encrypted value as a number.
 */
@CompileStatic
class GormEncryptedBigIntegerType extends AbstractEncryptedUserType<BigInteger, PBEBigIntegerEncryptor> {

    @Override
    Class<BigInteger> returnedClass() {
        BigInteger
    }

    @Override
    int getSqlType() {
        Types.NUMERIC
    }

    @Override
    protected PBEBigIntegerEncryptor createEncryptor(JasyptEncryptorParameters parameters) {
        parameters.createBigIntegerEncryptor()
    }

    @Override
    BigInteger nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        def integerEncryptor = encryptor
        def decimalMessage = rs.getBigDecimal(position)
        if (rs.wasNull()) {
            return null
        }
        integerEncryptor.decrypt(decimalMessage.setScale(0, RoundingMode.UNNECESSARY).unscaledValue())
    }

    @Override
    void nullSafeSet(PreparedStatement st, BigInteger value, int index, WrapperOptions options) throws SQLException {
        def integerEncryptor = encryptor
        if (value == null) {
            st.setNull(index, sqlType)
        } else {
            st.setBigDecimal(index, new BigDecimal(integerEncryptor.encrypt(value)))
        }
    }
}
