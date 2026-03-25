package com.tasktracker.auth.service;

import com.tasktracker.auth.domain.AppUser;
import com.tasktracker.auth.domain.Role;
import com.tasktracker.auth.repository.UserRepository;
import com.tasktracker.common.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username=%s was not found".formatted(username)));
    }

    public boolean isAdmin() {
        return getCurrentUser().getRole() == Role.ADMIN;
    }
}
