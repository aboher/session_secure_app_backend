package com.aboher.inventory.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "frontend")
@Getter
@AllArgsConstructor
public class FrontendProperties {
    private final String url;
    private final String userEmailConfirmationHandlerPath;
    private final String userPasswordChangePath;
    private final String requestPasswordChangePath;
}
