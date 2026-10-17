package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

import org.jasypt.commons.CommonUtils
import org.jasypt.hibernate5.type.ParameterNaming

@CompileStatic
class GormEncryptedCalendarAsStringType extends AbstractEncryptedAsStringType<Calendar> {

    private boolean storeTimeZone = false

    @Override
    Class<Calendar> returnedClass() {
        Calendar
    }

    @Override
    protected void configure(Properties parameters) {
        def paramStoreTimeZone = parameters.getProperty(ParameterNaming.STORE_TIME_ZONE)
        if (paramStoreTimeZone?.trim()) {
            storeTimeZone = CommonUtils.getStandardBooleanValue(paramStoreTimeZone)
        }
    }

    @Override
    protected Calendar convertToObject(String string) {
        def tokens = CommonUtils.split(string)
        def calendar = Calendar.instance
        calendar.timeZone = storeTimeZone ? TimeZone.getTimeZone(tokens[1]) : TimeZone.default
        calendar.timeInMillis = Long.parseLong(tokens[0])
        return calendar
    }

    @Override
    protected String convertToString(Calendar value) {
        def string = String.valueOf(value.timeInMillis)
        storeTimeZone ? "$string ${value.timeZone.ID}" : string
    }

    @Override
    Calendar deepCopy(Calendar value) {
        (Calendar) value?.clone()
    }

    @Override
    boolean isMutable() {
        true
    }
}
