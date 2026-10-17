package com.bloomhealthco.jasypt

import java.sql.PreparedStatement
import java.sql.ResultSet

import org.jasypt.encryption.pbe.StandardPBEBigDecimalEncryptor
import org.jasypt.encryption.pbe.StandardPBEBigIntegerEncryptor
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.exceptions.EncryptionInitializationException
import org.jasypt.hibernate5.encryptor.HibernatePBEBigDecimalEncryptor
import org.jasypt.hibernate5.encryptor.HibernatePBEBigIntegerEncryptor
import org.jasypt.hibernate5.encryptor.HibernatePBEByteEncryptor
import org.jasypt.hibernate5.encryptor.HibernatePBEStringEncryptor
import spock.lang.Shared
import spock.lang.Specification

class GormEncryptedTypesSpec extends Specification {

    static final String PASSWORD = 's3kr1t'

    @Shared
    def stringEncryptor = new StandardPBEStringEncryptor(password: PASSWORD)

    @Shared
    def bigDecimalEncryptor = new StandardPBEBigDecimalEncryptor(password: PASSWORD)

    @Shared
    def bigIntegerEncryptor = new StandardPBEBigIntegerEncryptor(password: PASSWORD)

    @Shared
    def byteEncryptor = new StandardPBEByteEncryptor(password: PASSWORD)

    void setupSpec() {
        // Without a jasypt config, the user types use the encryptors registered as 'gormEncryptor'
        new HibernatePBEStringEncryptor(registeredName: 'gormEncryptor', encryptor: stringEncryptor)
        new HibernatePBEBigDecimalEncryptor(registeredName: 'gormEncryptor', encryptor: bigDecimalEncryptor)
        new HibernatePBEBigIntegerEncryptor(registeredName: 'gormEncryptor', encryptor: bigIntegerEncryptor)
        new HibernatePBEByteEncryptor(registeredName: 'gormEncryptor', encryptor: byteEncryptor)
    }

    void '#type.simpleName stores #value encrypted as a string'() {
        given:
        def userType = type.getDeclaredConstructor().newInstance()
        userType.setParameterValues(new Properties())
        def statement = Mock(PreparedStatement)
        String stored = null

        when:
        userType.nullSafeSet(statement, value, 1, null)

        then:
        1 * statement.setString(1, _ as String) >> { args -> stored = args[1] }
        stored != value.toString()

        when:
        def read = userType.nullSafeGet(resultSet(getString: stored), 1, null)

        then:
        read == value
        read.getClass() == userType.returnedClass()

        where:
        type                                | value
        GormEncryptedStringType             | 'foo'
        GormEncryptedBooleanType            | true
        GormEncryptedByteAsStringType       | (byte) 2
        GormEncryptedShortAsStringType      | (short) 160
        GormEncryptedIntegerAsStringType    | 74
        GormEncryptedLongAsStringType       | 1234567890L
        GormEncryptedFloatAsStringType      | 123.45f
        GormEncryptedDoubleAsStringType     | 85.0d
        GormEncryptedBigDecimalAsStringType | 3.33G
        GormEncryptedBigIntegerAsStringType | 12345678901234567890G
        GormEncryptedDateAsStringType       | new Date(1234567890L)
    }

    void 'stored values are plain jasypt values, compatible with the Hibernate 5 flavour'() {
        given:
        def userType = new GormEncryptedDateAsStringType()
        userType.setParameterValues(null)
        def statement = Mock(PreparedStatement)
        String stored = null

        when:
        userType.nullSafeSet(statement, new Date(1234567890L), 1, null)

        then:
        1 * statement.setString(1, _ as String) >> { args -> stored = args[1] }
        stringEncryptor.decrypt(stored) == '1234567890'

        and:
        userType.nullSafeGet(resultSet(getString: stringEncryptor.encrypt('1234567890')), 1, null) == new Date(1234567890L)
    }

    void 'calendars are stored with the time zone when storeTimeZone is set'() {
        given:
        def userType = new GormEncryptedCalendarAsStringType()
        def parameters = new Properties()
        parameters.setProperty('storeTimeZone', 'true')
        userType.setParameterValues(parameters)
        def value = Calendar.getInstance(TimeZone.getTimeZone('Europe/Copenhagen'))
        value.timeInMillis = 1234567890L
        def statement = Mock(PreparedStatement)
        String stored = null

        when:
        userType.nullSafeSet(statement, value, 1, null)

        then:
        1 * statement.setString(1, _ as String) >> { args -> stored = args[1] }
        stringEncryptor.decrypt(stored) == '1234567890 Europe/Copenhagen'

        when:
        Calendar read = userType.nullSafeGet(resultSet(getString: stored), 1, null)

        then:
        read.timeInMillis == 1234567890L
        read.timeZone.ID == 'Europe/Copenhagen'
    }

    void 'big decimals are stored encrypted as numbers with the given scale'() {
        given:
        def userType = new GormEncryptedBigDecimalType()
        def parameters = new Properties()
        parameters.setProperty('decimalScale', '2')
        userType.setParameterValues(parameters)
        def statement = Mock(PreparedStatement)
        BigDecimal stored = null

        when:
        userType.nullSafeSet(statement, 3.339G, 1, null)

        then:
        1 * statement.setBigDecimal(1, _ as BigDecimal) >> { args -> stored = args[1] }
        bigDecimalEncryptor.decrypt(stored) == 3.33G

        expect:
        userType.nullSafeGet(resultSet(getBigDecimal: stored), 1, null) == 3.33G
    }

    void 'big decimals require the decimalScale parameter'() {
        when:
        new GormEncryptedBigDecimalType().setParameterValues(new Properties())

        then:
        def e = thrown(EncryptionInitializationException)
        e.message == 'decimalScale must be specified'
    }

    void 'big integers are stored encrypted as numbers'() {
        given:
        def userType = new GormEncryptedBigIntegerType()
        userType.setParameterValues(new Properties())
        def statement = Mock(PreparedStatement)
        BigDecimal stored = null

        when:
        userType.nullSafeSet(statement, 12345678901234567890G, 1, null)

        then:
        1 * statement.setBigDecimal(1, _ as BigDecimal) >> { args -> stored = args[1] }
        bigIntegerEncryptor.decrypt(stored.toBigIntegerExact()) == 12345678901234567890G

        expect:
        userType.nullSafeGet(resultSet(getBigDecimal: stored), 1, null) == 12345678901234567890G
    }

    void 'binary values are stored encrypted'() {
        given:
        def userType = new GormEncryptedBinaryType()
        userType.setParameterValues(new Properties())
        def value = [1, 2, 3] as byte[]
        def statement = Mock(PreparedStatement)
        byte[] stored = null

        when:
        userType.nullSafeSet(statement, value, 1, null)

        then:
        1 * statement.setBytes(1, _ as byte[]) >> { args -> stored = args[1] }
        byteEncryptor.decrypt(stored) == value

        expect:
        userType.nullSafeGet(resultSet(getBytes: stored), 1, null) == value
        userType.equals(value, [1, 2, 3] as byte[])
        !userType.deepCopy(value).is(value)
    }

    void 'null values are stored as null'() {
        given:
        def userType = new GormEncryptedStringType()
        userType.setParameterValues(new Properties())
        def statement = Mock(PreparedStatement)

        when:
        userType.nullSafeSet(statement, null, 1, null)

        then:
        1 * statement.setNull(1, java.sql.Types.VARCHAR)

        expect:
        userType.nullSafeGet(resultSet(getString: null, wasNull: true), 1, null) == null
    }

    void 'an encryptor name and a password cannot both be given'() {
        given:
        def parameters = new Properties()
        parameters.setProperty('password', PASSWORD)

        when:
        new GormEncryptedStringType().setParameterValues(parameters)

        then:
        def e = thrown(EncryptionInitializationException)
        e.message.startsWith('If "encryptorRegisteredName" is specified')
    }

    void 'an unknown encryptor name fails when the encryptor is used'() {
        given:
        def userType = new GormEncryptedStringType()
        def parameters = new Properties()
        parameters.setProperty('encryptorRegisteredName', 'unknown')
        userType.setParameterValues(parameters)

        when:
        userType.nullSafeSet(Mock(PreparedStatement), 'foo', 1, null)

        then:
        def e = thrown(EncryptionInitializationException)
        e.message == 'No string encryptor registered for hibernate with name "unknown"'
    }

    private ResultSet resultSet(Map<String, Object> values) {
        Stub(ResultSet) {
            getString(1) >> values.getString
            getBigDecimal(1) >> values.getBigDecimal
            getBytes(1) >> values.getBytes
            wasNull() >> (values.wasNull ?: false)
        }
    }
}
