package com.example.jwt_demo.service;

import com.example.jwt_demo.exception.InvalidDataException;
import com.example.jwt_demo.model.Role;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.UserRepository;
import com.example.jwt_demo.utils.ValidationUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Fetch current user by unique username
    public User getCurrentUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }
        return user;
    }

    // Fetch user by ID
    public User getUser(Long id, UserDetails userDetails) {
        User requestedUser = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        User currentUser = userRepository.findByUsername(userDetails.getUsername());
        if (!currentUser.getId().equals(id) && !currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
            throw new SecurityException("Unauthorized access");
        }
        return requestedUser;
    }

    // Update user (self or admin)
    public void updateUser(Long id, User updated, UserDetails userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        User currentUser = userRepository.findByUsername(userDetails.getUsername());
        if (!currentUser.getId().equals(id) && !currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
            throw new SecurityException("Unauthorized access");
        }
        if (updated.getEmail() != null && !updated.getEmail().trim().isEmpty()) {
            // Validate email format
            if (!ValidationUtils.isValidEmail(updated.getEmail())) {
                throw new InvalidDataException("Invalid email format");
            }
            if (!user.getEmail().equals(updated.getEmail()) && userRepository.existsByEmail(updated.getEmail())) {
                throw new InvalidDataException("Email is already taken");
            }
            user.setEmail(updated.getEmail());
        }
//        if (userReference.getUsername() != null && !userReference.getUsername().trim().isEmpty()) {
//            user.setUsername(userReference.getUsername());
//        }
//        if (userReference.getFirstName() != null && !userReference.getFirstName().trim().isEmpty()) {
//            user.setFirstName(userReference.getFirstName());
//        }
//        if (userReference.getLastName() != null && !userReference.getLastName().trim().isEmpty()) {
//            user.setLastName(userReference.getLastName());
//        }
        updateField(updated.getUsername(), user::setUsername, "Username");
        updateField(updated.getFirstName(), user::setFirstName, "First name");
        updateField(updated.getLastName(), user::setLastName, "Last name");
        userRepository.save(user);
    }

    // Delete user (self or admin)
    public void deleteUser(Long id, UserDetails userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        User currentUser = userRepository.findByUsername(userDetails.getUsername());
        boolean isSelf = currentUser.getId().equals(id);
        boolean isAdmin = currentUser.getRoles().contains(Role.ROLE_ADMIN);
        if (!isSelf && !isAdmin) {
            throw new SecurityException("Unauthorized access");
        }
        // Prevent deleting the only admin
        if (user.getRoles().contains(Role.ROLE_ADMIN)) {
            long adminCount = userRepository.countByRolesContaining(Role.ROLE_ADMIN);
            if (adminCount == 1) {
                throw new SecurityException("Cannot delete the only admin account!");
            }
        }
        userRepository.deleteById(id);
    }
    
    // helper method
    private void updateField(String value, Consumer<String> setter, String name) {
        if (value != null) {
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                throw new InvalidDataException(name + " cannot be empty");
            }
            setter.accept(trimmed);
        }
    }

}

