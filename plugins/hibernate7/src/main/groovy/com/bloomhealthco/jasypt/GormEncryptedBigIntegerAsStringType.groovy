package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedBigIntegerAsStringType extends AbstractEncryptedAsStringType<BigInteger> {

    @Override
    Class<BigInteger> returnedClass() {
        BigInteger
    }

    @Override
    protected BigInteger convertToObject(String string) {
        new BigInteger(string)
    }
}
