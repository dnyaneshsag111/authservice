package com.examle.authservice.configuration;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

  private final JwtAuthConverter jwtAuthConverter = new JwtAuthConverter();

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    return http.csrf(AbstractHttpConfigurer::disable)

        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login").permitAll() // Allow access to /login
            .anyRequest().authenticated()         // Secure all other endpoints
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2Login(Customizer.withDefaults())
        .build();

  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
      Collection<GrantedAuthority> authorities = new ArrayList<>();

      extractRolesFromClaim(jwt, authorities);
      extractRolesFromClaim(jwt, "resource_access", authorities, "auth-realm-client");

      return authorities;
    });

    return jwtAuthenticationConverter;
  }

  private void extractRolesFromClaim(Jwt jwt, Collection<GrantedAuthority> authorities) {
    extractRolesFromClaim(jwt, "realm_access", authorities, null);
  }

  private void extractRolesFromClaim(
      Jwt jwt, String claimName, Collection<GrantedAuthority> authorities, String resource
  ) {
    Map<String, Object> claim = jwt.getClaim(claimName);
    if (claim != null && claim.containsKey("roles")) {
      List<String> roles = (List<String>) claim.get("roles");
      for (String role : roles) {
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
      }
    }

    if (resource != null) {
      Map<String, Object> resourceAccess = jwt.getClaim(claimName);
      if (resourceAccess != null && resourceAccess.containsKey(resource)) {
        List<String> resourceRoles = (List<String>) ((Map<String, Object>) resourceAccess.get(resource)).get("roles");
        if (resourceRoles != null) {
          for (String role : resourceRoles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
          }
        }
      }
    }
  }
}
