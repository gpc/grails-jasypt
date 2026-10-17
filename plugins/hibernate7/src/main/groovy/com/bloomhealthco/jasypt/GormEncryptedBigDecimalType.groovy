package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

import java.math.RoundingMode
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

import org.hibernate.type.descriptor.WrapperOptions
import org.jasypt.encryption.pbe.PBEBigDecimalEncryptor
import org.jasypt.exceptions.EncryptionInitializationException
import org.jasypt.hibernate5.type.ParameterNaming

/**
 * Stores the encrypted value as a number. Requires the decimalScale parameter.
 */
@CompileStatic
class GormEncryptedBigDecimalType extends AbstractEncryptedUserType<BigDecimal, PBEBigDecimalEncryptor> {

    private Integer decimalScale

    @Override
    Class<BigDecimal> returnedClass() {
        BigDecimal
    }

    @Override
    int getSqlType() {
        Types.NUMERIC
    }

    @Override
    protected void configure(Properties parameters) {
        def paramDecimalScale = parameters.getProperty(ParameterNaming.DECIMAL_SCALE)
        if (paramDecimalScale == null) {
            throw new EncryptionInitializationException("${ParameterNaming.DECIMAL_SCALE} must be specified")
        }
        decimalScale = JasyptEncryptorParameters.parseInteger(ParameterNaming.DECIMAL_SCALE, paramDecimalScale)
    }

    @Override
    protected PBEBigDecimalEncryptor createEncryptor(JasyptEncryptorParameters parameters) {
        parameters.createBigDecimalEncryptor()
    }

    @Override
    BigDecimal nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        def decimalEncryptor = encryptor
        def storedEncryptedMessage = rs.getBigDecimal(position)
        if (rs.wasNull()) {
            return null
        }
        decimalEncryptor.decrypt(storedEncryptedMessage.setScale(decimalScale, RoundingMode.UNNECESSARY))
    }

    @Override
    void nullSafeSet(PreparedStatement st, BigDecimal value, int index, WrapperOptions options) throws SQLException {
        def decimalEncryptor = encryptor
        if (value == null) {
            st.setNull(index, sqlType)
        } else {
            st.setBigDecimal(index, decimalEncryptor.encrypt(value.setScale(decimalScale, RoundingMode.DOWN)))
        }
    }
}
