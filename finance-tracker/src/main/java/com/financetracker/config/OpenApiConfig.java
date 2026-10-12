package com.financetracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * Configures the SpringDoc OpenAPI documentation with JWT bearer authentication
 * and server URL definitions.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Produces the OpenAPI bean with title, version, JWT security scheme, and server URLs.
     *
     * @return configured OpenAPI instance
     */
    @Bean
    public OpenAPI openAPI() {
        var jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("bearerAuth");

        var securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("Finance Tracker API")
                        .version("v1")
                        .description("""
                                REST API for personal finance management.

                                **Authentication**: All endpoints except `/api/v1/auth/register` \
                                and `/api/v1/auth/login` require a JWT bearer token. \
                                Click **Authorize** and enter `Bearer <your_token>` to authenticate.

                                **Pagination**: List endpoints accept `page` (0-indexed) and \
                                `size` (max 25) query parameters.

                                **Amounts**: All monetary values are `BigDecimal` serialized as \
                                JSON numbers (e.g. `49.99`).
                                """)
                        .contact(new Contact()
                                .name("Finance Tracker")
                                .email("support@financetracker.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", jwtScheme))
                .addSecurityItem(securityRequirement)
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Dev server"),
                        new Server().url("https://api.financetracker.com").description("Prod server")
                ));
    }
}
