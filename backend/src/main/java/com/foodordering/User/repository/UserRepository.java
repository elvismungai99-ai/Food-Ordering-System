package com.foodordering.User.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodordering.User.entity.User;

public interface UserRepository
        extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(
            String email
    );

    boolean existsByEmail(
            String email
    );

    /*
     * Required by AdminController.
     */
    List<User> findByRole(
            String role
    );
}