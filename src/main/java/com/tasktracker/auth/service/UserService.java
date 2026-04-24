package com.tasktracker.auth.service;

import com.tasktracker.auth.api.UserResponse;
import com.tasktracker.auth.api.UserRoleUpdateRequest;
import com.tasktracker.auth.domain.AppUser;
import com.tasktracker.auth.domain.Role;
import com.tasktracker.auth.repository.UserRepository;
import com.tasktracker.auth.repository.UserSpecifications;
import com.tasktracker.common.api.PageResponse;
import com.tasktracker.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public UserService(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    public UserResponse currentUser() {
        return UserMapper.toResponse(currentUserService.getCurrentUser());
    }

    public PageResponse<UserResponse> findAll(String query, Role role, Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(
                        UserSpecifications.usernameContains(query)
                                .and(UserSpecifications.hasRole(role)),
                        pageable
                )
                .map(UserMapper::toResponse);
        return PageResponse.from(page);
    }

    public UserResponse findById(Long id) {
        return UserMapper.toResponse(getUser(id));
    }

    @Transactional
    public UserResponse updateRole(Long id, UserRoleUpdateRequest request) {
        if (!currentUserService.isAdmin()) {
            throw new AccessDeniedException("Only admins can change user roles");
        }

        AppUser user = getUser(id);
        AppUser currentUser = currentUserService.getCurrentUser();
        if (currentUser.getId().equals(user.getId()) && request.role() != Role.ADMIN) {
            throw new IllegalArgumentException("Admin cannot remove own admin role");
        }

        user.setRole(request.role());
        return UserMapper.toResponse(user);
    }

    private AppUser getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id=%d was not found".formatted(id)));
    }
}
