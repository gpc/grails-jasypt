package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedStringType extends JasyptConfiguredUserType<org.jasypt.hibernate5.type.EncryptedStringType> {
}
