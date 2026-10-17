package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedBigDecimalAsStringType extends AbstractEncryptedAsStringType<BigDecimal> {

    @Override
    Class<BigDecimal> returnedClass() {
        BigDecimal
    }

    @Override
    protected BigDecimal convertToObject(String string) {
        new BigDecimal(string)
    }
}
