package com.examle.authservice.controller;

import com.examle.authservice.dto.LoginRequest;
import com.examle.authservice.properties.KeycloakProperties;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;



@RestController
@RequestMapping("auth")
public class AuthController {
  @Autowired
  Keycloak keycloak;

  @Autowired
  KeycloakProperties keycloakProperties;

  @Autowired
  RestTemplate restTemplate;

  @PostMapping("/login")
  public ResponseEntity<Object> login(@RequestBody LoginRequest credentials) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    // Keycloak token endpoint
    String tokenUrl = UriComponentsBuilder.newInstance()
        .scheme(keycloakProperties.isUseHttps() ? "https" : "http")
        .host(keycloakProperties.getHostname())
        .port(keycloakProperties.getPort())
        .path(keycloakProperties.getAuthUrl() + "/protocol/openid-connect/token")
        .toUriString();
    // Prepare request body using MultiValueMap
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("client_id", keycloakProperties.getClientId());
    body.add("client_secret", keycloakProperties.getClientSecret());
    body.add("username", credentials.getUsername());
    body.add("password", credentials.getPassword());

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    Object response = restTemplate.postForObject(tokenUrl, request, Object.class);

    // Return the token or error
    return ResponseEntity.ok(response);
  }
}
