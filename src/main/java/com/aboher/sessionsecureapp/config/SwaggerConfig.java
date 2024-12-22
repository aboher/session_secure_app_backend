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
                .in(SecurityScheme.In.COOKIE)
                .description("""
                        The authentication is tracked by a session cookie called
                        'SESSION'. There are three different roles: USER,
                        MODERATOR and ADMIN. For some endpoints a specific role
                        may be needed.
                        """);

        SecurityScheme csrfHeaderScheme = new SecurityScheme()
                .name("X-XSRF-TOKEN")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .description("""
                        CSRF token required for state-changing requests:
                        POST, PUT, PATCH and DELETE. The token is send to the client
                        by a cookie named 'XSRF-TOKEN' and must be added in
                        a header named 'X-XSRF-TOKEN' for the server to accept
                        the request.
                        """);

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("session-cookie", sessionCookieScheme)
                        .addSecuritySchemes("csrf-token", csrfHeaderScheme))
                .info(new Info()
                        .title("Session Security API")
                        .version("1.0")
                        .description("Operations related to users and session security"));
    }
}
