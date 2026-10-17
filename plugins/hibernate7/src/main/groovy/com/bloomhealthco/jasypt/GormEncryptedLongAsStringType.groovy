package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedLongAsStringType extends AbstractEncryptedAsStringType<Long> {

    @Override
    Class<Long> returnedClass() {
        Long
    }

    @Override
    protected Long convertToObject(String string) {
        Long.valueOf(string)
    }
}
