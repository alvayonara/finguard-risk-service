package com.alvayonara.finguardriskservice.security;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  private static final String[] SWAGGER_PATHS = {
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/swagger-ui/index.html",
    "/v3/api-docs",
    "/v3/api-docs/**",
    "/webjars/**"
  };

  private final String allowedPublicPaths;
  private final Environment environment;

  public SecurityConfig(
      @Value("${security.allowedPublicPaths:}") String allowedPublicPaths,
      Environment environment) {
    this.allowedPublicPaths = allowedPublicPaths;
    this.environment = environment;
  }

  @Bean
  @Order(1)
  @Profile("stg")
  public SecurityWebFilterChain swaggerSecurityFilterChain(ServerHttpSecurity http) {
    return http.securityMatcher(ServerWebExchangeMatchers.pathMatchers(SWAGGER_PATHS))
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
        .build();
  }

  @Bean
  @Order(2)
  public SecurityWebFilterChain securityWebFilterChain(
      ServerHttpSecurity http,
      ReactiveJwtDecoder jwtDecoder,
      ReactiveJwtAuthenticationConverter jwtAuthConverter) {
    log.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchanges -> {
              Arrays.stream(allowedPublicPaths.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .forEach(url -> exchanges.pathMatchers(url).permitAll());
              exchanges.anyExchange().authenticated();
            })
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.jwtDecoder(jwtDecoder)
                            .jwtAuthenticationConverter(jwtAuthConverter)))
        .build();
  }
}
