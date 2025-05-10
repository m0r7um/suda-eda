package org.food.sudaeda.configuration;

import org.food.sudaeda.jca.KeycloakConnectionFactory;
import org.food.sudaeda.jca.KeycloakManagedConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakJcaConfig {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Bean
    public KeycloakConnectionFactory keycloakConnectionFactory() {
        KeycloakManagedConnectionFactory factory = new KeycloakManagedConnectionFactory();
        factory.setServerUrl(serverUrl);
        factory.setRealm(realm);
        factory.setClientId(clientId);
        factory.setClientSecret(clientSecret);
        return (KeycloakConnectionFactory) factory.createConnectionFactory(null);
    }
}
