package com.bloomhealthco.domain

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import spock.lang.Specification

import javax.sql.DataSource

@Integration
@Rollback
class JasyptDomainEncryptionTests extends Specification {

    DataSource dataSource

    String CORRELATION_ID = "ABC123"

    void "testStringStringEncryption"() {
        expect:
        testPropertyAsStringEncryption('firstName', 'FIRST_NAME', 'foo')
    }

    void "testDateStringEncryption"() {
        expect:
        testPropertyAsStringEncryption('birthDate', 'BIRTH_DATE', new Date(1970, 2, 3))
    }

    void "testDoubleStringEncryption"() {
        expect:
        testPropertyAsStringEncryption('latitude', 'LATITUDE', 85.0d)
    }

    void "testBigDecimalEncryption"() {
        given:
        def originalPatient = new Patient(correlationId: CORRELATION_ID)
        originalPatient.bdBalance = 3.325
        originalPatient.save(failOnError: true)

        when:
        originalPatient = Patient.findByCorrelationId(CORRELATION_ID)

        then:
        originalPatient.bdBalance == 3.33 // property's scale is set to 2
    }

    void "testBooleanStringEncryption"() {
        expect:
        testPropertyAsStringEncryption('hasInsurance', 'HAS_INSURANCE', true)
    }

    void "testFloatStringEncryption"() {
        expect:
        testPropertyAsStringEncryption('cashBalance', 'CASH_BALANCE', 123.45f)
    }

    void "testShortStringEncryption"() {
        expect:
        testPropertyAsStringEncryption('weight', 'WEIGHT', 160)
    }

    void "testIntegerStringEncryption"() {
        expect:
        testPropertyAsStringEncryption('height', 'HEIGHT', 74)
    }

    void "testLongStringEncryption"() {
        expect:
        testPropertyAsStringEncryption('patientId', 'PATIENT_ID', 1234567890)
    }

    void "testByteStringEncryption"() {
        expect:
        testPropertyAsStringEncryption('biteMe', 'BITE_ME', 2)
    }

    void "testSaltingEncryptsSameValueDifferentlyEachTime"() {
        given:
        def originalPatient = new Patient(firstName: "foo", lastName: "foo", correlationId: CORRELATION_ID)
        originalPatient.save(failOnError: "true")

        withPatientForCorrelationId(CORRELATION_ID) { patient, rawPatient ->
            expect:
            "foo" == patient.firstName
            "foo" == patient.lastName
            "foo" != rawPatient.FIRST_NAME
            "foo" != rawPatient.LAST_NAME
            rawPatient.FIRST_NAME != rawPatient.LAST_NAME
        }
    }

    void "testEncryptionWithLongNamesFit"() {
        given:
        def LONG_NAME_256 = "ABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOPABCDEFGHIJKLMNOP"

        (1..256).each { val ->
            def firstName = LONG_NAME_256.substring(0, val)
            new Patient(firstName: firstName, correlationId: val, lastName: "foo").save(failOnError: true)
            withPatientForCorrelationId(val as String) { patient, rawPatient ->
                expect:
                patient
                firstName == patient.firstName
                // Bouncy Castle AES block encryption encrypts 256 character string in 384 characters
                rawPatient.FIRST_NAME.size() <= 384
            }
        }
    }

    boolean testPropertyAsStringEncryption(property, rawProperty, value) {
        def originalPatient = new Patient(correlationId: CORRELATION_ID)
        originalPatient."$property" = value
        originalPatient.save(failOnError: "true")

        withPatientForCorrelationId(CORRELATION_ID) { patient, rawPatient ->
            assert value == patient."$property"
            def rawPropertyValue = rawPatient."$rawProperty"
            assert value.toString() != rawPropertyValue
            assert rawPropertyValue.endsWith("=")
        }
        return true
    }

    def withPatientForCorrelationId(String correlationId, Closure closure) {
        def patient = Patient.findByCorrelationId(correlationId)
        assert patient
        retrieveRawPatientFromDatabase(correlationId) { rawPatient ->
            closure(patient, rawPatient)
        }
    }

    def retrieveRawPatientFromDatabase(correlationId, closure) {
        new Sql(dataSource).with { db ->
            try {
                def result = db.firstRow("SELECT * FROM patient where correlation_id = $correlationId")
                closure(result)
            } finally {
                db.close()
            }
        }
    }
}
