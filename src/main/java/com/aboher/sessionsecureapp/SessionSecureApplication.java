package com.aboher.sessionsecureapp;

import com.aboher.sessionsecureapp.config.FrontendProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FrontendProperties.class)
public class SessionSecureApplication {

    public static void main(String[] args) {
        SpringApplication.run(SessionSecureApplication.class, args);
    }

}
