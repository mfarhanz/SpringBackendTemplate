package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.Role;
import com.example.jwt_demo.model.User;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByRolesContaining(Role role);
    List<User> findAllByEmailVerifiedFalseAndDateCreatedBefore(LocalDateTime cutoff);
    List<User> findAllByEmailVerifiedFalseAndDateCreatedBetween(LocalDateTime start, LocalDateTime end);
}
