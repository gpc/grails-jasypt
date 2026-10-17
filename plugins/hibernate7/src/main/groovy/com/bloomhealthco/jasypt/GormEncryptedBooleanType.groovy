package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedBooleanType extends AbstractEncryptedAsStringType<Boolean> {

    @Override
    Class<Boolean> returnedClass() {
        Boolean
    }

    @Override
    protected Boolean convertToObject(String string) {
        Boolean.valueOf(string)
    }
}
