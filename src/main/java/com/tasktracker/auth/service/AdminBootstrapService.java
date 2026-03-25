package com.tasktracker.auth.service;

import com.tasktracker.auth.config.AdminBootstrapProperties;
import com.tasktracker.auth.domain.AppUser;
import com.tasktracker.auth.domain.Role;
import com.tasktracker.auth.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapService {

    @Bean
    ApplicationRunner adminBootstrapRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AdminBootstrapProperties adminBootstrapProperties
    ) {
        return args -> {
            if (userRepository.existsByUsername(adminBootstrapProperties.username())) {
                return;
            }

            AppUser admin = new AppUser();
            admin.setUsername(adminBootstrapProperties.username());
            admin.setPassword(passwordEncoder.encode(adminBootstrapProperties.password()));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        };
    }
}
