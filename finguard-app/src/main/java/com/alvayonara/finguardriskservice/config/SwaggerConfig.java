package com.alvayonara.finguardriskservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("stg")
public class SwaggerConfig {
  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
                .title("FinGuard Risk Service API")
                .description("REST API documentation for FinGuard Risk Service")
                .version("v1.0.0")
                .contact(new Contact().name("Alva Studio")))
        .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .components(
            new Components()
                .addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Paste your JWT access token here")));
  }
}

