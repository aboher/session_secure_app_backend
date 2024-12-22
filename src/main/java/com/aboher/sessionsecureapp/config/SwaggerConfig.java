package com.aboher.sessionsecureapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme sessionCookieScheme = new SecurityScheme()
                .name("SESSION")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE);

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("SESSION", sessionCookieScheme))
                .info(new Info()
                        .title("Session Security API")
                        .version("1.0")
                        .description("Operations related to users and session security"));
    }
}
