package com.tasktracker.auth.service;

import com.tasktracker.auth.api.AuthResponse;
import com.tasktracker.auth.api.LoginRequest;
import com.tasktracker.auth.api.RegisterRequest;
import com.tasktracker.auth.api.UserProfileResponse;
import com.tasktracker.auth.domain.AppUser;
import com.tasktracker.auth.domain.Role;
import com.tasktracker.auth.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CurrentUserService currentUserService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        AppUser savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getUsername(), savedUser.getRole());
        return new AuthResponse(token, savedUser.getUsername(), savedUser.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow();
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public UserProfileResponse currentUser() {
        AppUser user = currentUserService.getCurrentUser();
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getRole().name(), user.getCreatedAt());
    }
}
