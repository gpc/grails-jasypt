package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedShortAsStringType extends AbstractEncryptedAsStringType<Short> {

    @Override
    Class<Short> returnedClass() {
        Short
    }

    @Override
    protected Short convertToObject(String string) {
        Short.valueOf(string)
    }
}
