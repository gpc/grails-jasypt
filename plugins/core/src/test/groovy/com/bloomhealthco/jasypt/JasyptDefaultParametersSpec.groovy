package com.bloomhealthco.jasypt

import spock.lang.Specification

class JasyptDefaultParametersSpec extends Specification {

    void 'defaults the encryptor name when no encryptor is configured'() {
        expect:
        JasyptDefaultParameters.defaultParameters(config) == expected

        where:
        config                                     | expected
        null                                       | [encryptorRegisteredName: 'gormEncryptor']
        [:]                                        | [encryptorRegisteredName: 'gormEncryptor']
        [providerName: 'BC']                       | [providerName: 'BC', encryptorRegisteredName: 'gormEncryptor']
        [encryptorRegisteredName: 'fooBar']        | [encryptorRegisteredName: 'fooBar']
        [password: 's3kr1t']                       | [password: 's3kr1t']
        [algorithm: 'PBEWithMD5AndTripleDES']      | [algorithm: 'PBEWithMD5AndTripleDES']
        [keyObtentionIterations: 1000]             | [keyObtentionIterations: 1000]
    }

    void 'does not modify the given config'() {
        given:
        def config = [providerName: 'BC'] as Map<String, Object>

        when:
        JasyptDefaultParameters.defaultParameters(config)

        then:
        config == [providerName: 'BC']
    }
}
