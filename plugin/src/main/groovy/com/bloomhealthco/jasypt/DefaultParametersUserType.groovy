package com.bloomhealthco.jasypt

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.hibernate.engine.spi.SessionImplementor

import java.sql.PreparedStatement
import java.sql.ResultSet

import org.hibernate.usertype.ParameterizedType
import org.hibernate.usertype.UserType

/**
 * A wrapper class that supports providing default parameters to UserTypes
 * implement the getDefaultParameters to provide the configuration you want
 *
 * We use composition so that we can add this behavior to otherwise declared final UserTypes.
 * We'd love to use a Mixin but Hibernate's reflection utils don't play nice with Groovys ryntime mixins
 * @param <T >  the existing Parameterized UserType we want to add the default parmaters to
 */
//abstract class DefaultParametersUserType<T extends UserType & ParameterizedType> implements UserType, ParameterizedType {
@CompileStatic
abstract class DefaultParametersUserType<T extends UserType> implements UserType, ParameterizedType {

    // Nifty little reflection trick to figure out the actual class the subclass provides
    // UserTypes have to have a default parameterless constructor so we're safe to call it here
    protected final T innerType = (T) ((Class<T>) (((java.lang.reflect.ParameterizedType) getClass().genericSuperclass).actualTypeArguments[0])).newInstance()

    int[] sqlTypes() {
        innerType.sqlTypes()
    }

    Class returnedClass() {
        innerType.returnedClass()
    }

    boolean equals(x, y) {
        innerType.equals(x, y)
    }

    int hashCode(x) {
        innerType.hashCode(x)
    }

    def nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor sessionImplementor, owner) {
        innerType.nullSafeGet(resultSet, names, null, owner)
    }

    void nullSafeSet(PreparedStatement preparedStatement, value, int index, SessionImplementor sessionImplementor) {
        innerType.nullSafeSet(preparedStatement, value, index, null)
    }

    void nullSafeSet(java.sql.PreparedStatement preparedStatement, java.lang.Object value, int index, org.hibernate.engine.spi.SharedSessionContractImplementor sharedSessionContractImplementor) {
        innerType.nullSafeSet(preparedStatement, value, index, null)
    }

    Object nullSafeGet(java.sql.ResultSet resultSet, java.lang.String[] names, org.hibernate.engine.spi.SharedSessionContractImplementor sharedSessionContractImplementor, java.lang.Object owner) {
        innerType.nullSafeGet(resultSet, names, null, owner)
    }

    def deepCopy(value) {
        innerType.deepCopy(value)
    }

    boolean isMutable() {
        innerType.isMutable()
    }

    Serializable disassemble(value) {
        innerType.disassemble(value)
    }

    def assemble(Serializable cached, owner) {
        innerType.assemble(cached, owner)
    }

    def replace(original, target, owner) {
        innerType.replace(original, target, owner)
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    void setParameterValues(Properties properties) {
        def params = defaultParameters + (properties ?: [:]) as Properties
        ((ParameterizedType) innerType).setParameterValues(params)
    }

    abstract Map getDefaultParameters()
}
