package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedDoubleAsStringType extends AbstractEncryptedAsStringType<Double> {

    @Override
    Class<Double> returnedClass() {
        Double
    }

    @Override
    protected Double convertToObject(String string) {
        Double.valueOf(string)
    }
}
