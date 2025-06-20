package com.example.jwt_demo.controller;

import com.example.jwt_demo.dto.ApiResponse;
import com.example.jwt_demo.dto.UserDTO;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.UserRepository;
import com.example.jwt_demo.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	
    private final UserRepository userRepository;    
    private final UserService userService;
        
    // ADMIN ONLY: Get all users
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserDTO::new).collect(Collectors.toList());
    }
    
//    // READ: Get current user info
//    @GetMapping("/me")
//    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
//        User user = userRepository.findByUsername(userDetails.getUsername());
//        if (user == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>(new UserDTO(user), HttpStatus.OK);
//    }
    
    // READ: Get current user info
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(new UserDTO(user));
    }

//    // READ: Get a user by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getUserById(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
//        Optional<User> userOpt = userRepository.findById(id);
//        if (userOpt.isEmpty()) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        User requestedUser = userOpt.get();
//        User currentUser = userRepository.findByUsername(userDetails.getUsername());
//        if (!currentUser.getId().equals(id) && !currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
//            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
//        }
//        return new ResponseEntity<>(new UserDTO(requestedUser), HttpStatus.OK);
//    }
    
    // READ: Get a user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUser(id, userDetails);
        return ResponseEntity.ok(new UserDTO(user));
    }
    
//    // UPDATE: Update user (self or admin)
//    @PutMapping("/{id}")
//    public ResponseEntity<String> updateUser(
//            @PathVariable("id") Long id,
//            @RequestBody User userDetails,
//            @AuthenticationPrincipal UserDetails userPrincipal
//    ) {
//        Optional<User> userOptional = userRepository.findById(id);
//        if (userOptional.isEmpty()) {
//            return new ResponseEntity<>("User not found!", HttpStatus.NOT_FOUND);
//        }
//        User user = userOptional.get();
//        User currentUser = userRepository.findByUsername(userPrincipal.getUsername());
//        if (!currentUser.getId().equals(id) && !currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
//            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
//        }
//        if (userDetails.getUsername() != null && !userDetails.getUsername().isEmpty()) {
//            user.setUsername(userDetails.getUsername());
//        }
//        if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty()) {
//        	// Validate email format
//        	if (!ValidationUtils.isValidEmail(userDetails.getEmail())) {
//                return new ResponseEntity<>("Error: Invalid email format!", HttpStatus.BAD_REQUEST);
//            }
//        	// Check if email is already taken
//            if (!user.getEmail().equals(userDetails.getEmail()) && userRepository.existsByEmail(userDetails.getEmail())) {
//                return new ResponseEntity<>("Error: Email is already taken!", HttpStatus.BAD_REQUEST);
//            }
//            user.setEmail(userDetails.getEmail());
//        }
//        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
//            user.setPassword(encoder.encode(userDetails.getPassword()));
//        }
//        userRepository.save(user);
//        return new ResponseEntity<>("User updated successfully!", HttpStatus.OK);
//    }
    
    // UPDATE: Update user (self or admin)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable("id") Long id,
            @RequestBody User user,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        userService.updateUser(id, user, userDetails);
        return ResponseEntity.ok(new ApiResponse("User details updated successfully", HttpStatus.OK));
    }
    
//    // DELETE: Delete user (self or admin)
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteUser(
//            @PathVariable("id") Long id,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        Optional<User> userOptional = userRepository.findById(id);
//        if (userOptional.isEmpty()) {
//            return new ResponseEntity<>("User not found!", HttpStatus.NOT_FOUND);
//        }
//        User user = userOptional.get();
//        User currentUser = userRepository.findByUsername(userDetails.getUsername());
//        boolean isSelf = currentUser.getId().equals(id);
//        boolean isAdmin = currentUser.getRoles().contains(Role.ROLE_ADMIN);
//        if (!isSelf && !isAdmin) {
//            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
//        }
//        // Prevent deleting the only admin
//        if (user.getRoles().contains(Role.ROLE_ADMIN)) {
//            long adminCount = userRepository.countByRolesContaining(Role.ROLE_ADMIN);
//            if (adminCount == 1) {
//                return new ResponseEntity<>("Cannot delete the only admin account!", HttpStatus.FORBIDDEN);
//            }
//        }
//        userRepository.deleteById(id);
//        return new ResponseEntity<>("User deleted successfully!", HttpStatus.NO_CONTENT);
//    }
    
    // DELETE: Delete user (self or admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        userService.deleteUser(id, userDetails);
        ApiResponse response = new ApiResponse("User account deleted", HttpStatus.NO_CONTENT);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
