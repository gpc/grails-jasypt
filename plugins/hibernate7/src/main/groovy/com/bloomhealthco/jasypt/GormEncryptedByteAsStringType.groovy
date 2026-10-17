package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedByteAsStringType extends AbstractEncryptedAsStringType<Byte> {

    @Override
    Class<Byte> returnedClass() {
        Byte
    }

    @Override
    protected Byte convertToObject(String string) {
        Byte.valueOf(string)
    }
}
