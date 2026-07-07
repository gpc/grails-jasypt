package com.bloomhealthco.domain

import com.bloomhealthco.jasypt.*

class Patient {
	String firstName
	String lastName
    Date birthDate
    Calendar anniversary
    Double latitude
    Float cashBalance
    BigDecimal bdBalance
    Boolean hasInsurance
    Short weight
    Integer height
    Long patientId
    Byte biteMe
    String correlationId

    static constraints = {
        firstName nullable: true, maxSize: 384
        lastName nullable: true
        birthDate nullable: true
        anniversary nullable: true
        latitude nullable: true
        cashBalance nullable: true
        bdBalance nullable: true, scale: 2
        hasInsurance nullable: true
        weight nullable: true
        height nullable: true
        patientId nullable: true
        biteMe nullable: true
    }

	static mapping = {
    	firstName type: GormEncryptedStringType
        lastName type: GormEncryptedStringType
        birthDate type: GormEncryptedDateAsStringType
        anniversary type: GormEncryptedCalendarAsStringType
        hasInsurance type: GormEncryptedBooleanType
        latitude type: GormEncryptedDoubleAsStringType
        cashBalance type: GormEncryptedFloatAsStringType
        bdBalance type: GormEncryptedBigDecimalType
        bdBalance type: GormEncryptedBigDecimalAsStringType
        weight type: GormEncryptedShortAsStringType
        height type: GormEncryptedIntegerAsStringType
        patientId type: GormEncryptedLongAsStringType
        biteMe type: GormEncryptedByteAsStringType
    }
}
