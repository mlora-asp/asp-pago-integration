package com.asp.integration.testutil;

import com.asp.integration.infrastructure.config.properties.CaudexProperties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public final class CaudexTestProperties {

    private static final String ENDPOINTS_PREFIX = "providers.caudex.endpoints.";
    private static final String ENDPOINTS_BRACKET_PREFIX = "providers.caudex.endpoints[";

    private CaudexTestProperties() {
    }

    public static CaudexProperties fromApplicationYaml() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        Properties properties = yaml.getObject();

        CaudexProperties caudexProperties = new CaudexProperties();
        caudexProperties.setBaseUrl("http://localhost:9090");
        caudexProperties.setClientId("test-client-id");
        caudexProperties.setClientSecret("test-client-secret");
        caudexProperties.setScope("message.read");
        caudexProperties.setEndpoints(readEndpoints(properties));

        return caudexProperties;
    }

    private static Map<String, String> readEndpoints(Properties properties) {
        return properties.stringPropertyNames().stream()
                .filter(name -> name.startsWith(ENDPOINTS_PREFIX)
                        || name.startsWith(ENDPOINTS_BRACKET_PREFIX))
                .collect(Collectors.toMap(
                        CaudexTestProperties::operationTypeFromPropertyName,
                        properties::getProperty
                ));
    }

    private static String operationTypeFromPropertyName(String propertyName) {
        if (propertyName.startsWith(ENDPOINTS_BRACKET_PREFIX)) {
            return propertyName.substring(
                    ENDPOINTS_BRACKET_PREFIX.length(),
                    propertyName.length() - 1
            );
        }
        return propertyName.substring(ENDPOINTS_PREFIX.length());
    }
}
