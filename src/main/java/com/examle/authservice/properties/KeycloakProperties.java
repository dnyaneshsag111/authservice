package com.examle.authservice.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Data
public class KeycloakProperties {
  private String realm;
  private String authServerUrl;
  private String clientSecret;
  private String clientId;
  private String hostname;
  private int port;
  private boolean useHttps;
  private String authUrl;
}
