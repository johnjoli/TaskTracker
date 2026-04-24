package com.tasktracker.auth.repository;

import com.tasktracker.auth.domain.AppUser;
import com.tasktracker.auth.domain.Role;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<AppUser> hasRole(Role role) {
        return (root, query, criteriaBuilder) ->
                role == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("role"), role);
    }

    public static Specification<AppUser> usernameContains(String queryText) {
        return (root, query, criteriaBuilder) -> {
            if (queryText == null || queryText.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + queryText.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern);
        };
    }
}
