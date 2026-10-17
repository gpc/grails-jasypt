package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedStringType extends AbstractEncryptedAsStringType<String> {

    @Override
    Class<String> returnedClass() {
        String
    }

    @Override
    protected String convertToObject(String string) {
        string
    }
}
