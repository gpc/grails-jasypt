package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

@CompileStatic
class GormEncryptedDateAsStringType extends AbstractEncryptedAsStringType<Date> {

    @Override
    Class<Date> returnedClass() {
        Date
    }

    @Override
    protected Date convertToObject(String string) {
        new Date(Long.parseLong(string))
    }

    @Override
    protected String convertToString(Date value) {
        String.valueOf(value.time)
    }
}
