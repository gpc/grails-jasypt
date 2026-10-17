package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic

import org.hibernate.usertype.ParameterizedType
import org.hibernate.usertype.UserType

@CompileStatic
class JasyptConfiguredUserType<T extends UserType & ParameterizedType> extends DefaultParametersUserType<T> {

    /**
     * @return the default parameters, see {@link JasyptDefaultParameters#getDefaultParameters()}
     */
    Map getDefaultParameters() {
        JasyptDefaultParameters.defaultParameters
    }

    /**
     * @return the jasypt config, see {@link JasyptDefaultParameters#getJasyptConfig()}
     */
    Map<String, Object> getJasyptConfig() {
        JasyptDefaultParameters.jasyptConfig
    }
}
