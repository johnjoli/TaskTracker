package com.tasktracker.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record AdminBootstrapProperties(
        String username,
        String password
) {
}
