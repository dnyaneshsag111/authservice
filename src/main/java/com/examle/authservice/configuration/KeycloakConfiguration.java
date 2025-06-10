package com.examle.authservice.configuration;

import com.examle.authservice.properties.KeycloakProperties;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfiguration {

  @Autowired
  KeycloakProperties keycloakProperties;

  @Bean
  Keycloak keycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(keycloakProperties.getAuthServerUrl())
        .realm(keycloakProperties.getRealm())
        .clientId(keycloakProperties.getClientId())
        .clientSecret(keycloakProperties.getClientSecret())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .build();
  }

}
