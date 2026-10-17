package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedFloatAsStringType extends AbstractEncryptedAsStringType<Float> {

    @Override
    Class<Float> returnedClass() {
        Float
    }

    @Override
    protected Float convertToObject(String string) {
        Float.valueOf(string)
    }
}
