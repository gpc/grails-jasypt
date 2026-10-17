package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedIntegerAsStringType extends AbstractEncryptedAsStringType<Integer> {

    @Override
    Class<Integer> returnedClass() {
        Integer
    }

    @Override
    protected Integer convertToObject(String string) {
        Integer.valueOf(string)
    }
}
