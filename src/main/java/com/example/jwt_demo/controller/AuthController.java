package com.example.jwt_demo.controller;

import com.example.jwt_demo.annotations.LogEndpoint;
import com.example.jwt_demo.dto.ApiResponse;
import com.example.jwt_demo.dto.EmailRequest;
import com.example.jwt_demo.dto.AuthRequest;
import com.example.jwt_demo.dto.PasswordResetRequest;
import com.example.jwt_demo.dto.VerificationRequest;
import com.example.jwt_demo.dto.RefreshTokenRequest;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final AuthService authService;

    @PostMapping("/signin")
    @LogEndpoint(methodType = "POST")
    public ResponseEntity<ApiResponse> signIn(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
    	Map<String, Object> tokens = authService.authenticateUser(request);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK, tokens));
    }
    
    @PostMapping("/guest")
    @LogEndpoint(methodType = "POST")
    public ResponseEntity<ApiResponse> guestSignIn(HttpServletRequest request) {
        return ResponseEntity.ok(new ApiResponse("Logged in as Guest", HttpStatus.OK));
    }
    
    @PostMapping("/signout")
    @LogEndpoint(methodType = "POST")
    public ResponseEntity<ApiResponse> signOut(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
    	authService.logoutUser(request);
    	return ResponseEntity.ok(new ApiResponse("Log out successful", HttpStatus.OK));
    }
    
    @PostMapping("/signup")
    @LogEndpoint(methodType = "POST")
    public ResponseEntity<ApiResponse> signUp(@RequestBody User user, HttpServletRequest httpRequest) {
    	authService.registerUser(user);
    	ApiResponse response = new ApiResponse("Please verify your email to finish setting up your account", HttpStatus.CREATED);
    	return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @PostMapping("/request-email-verify")
    @LogEndpoint(methodType = "POST")
    public ResponseEntity<ApiResponse> requestEmailVerification(@RequestBody EmailRequest request, HttpServletRequest httpRequest) {
        authService.requestEmailVerification(request);
        ApiResponse response = new ApiResponse("Verification code sent. Please check your email.", HttpStatus.CREATED);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @PostMapping("/verify-email")
    @LogEndpoint(methodType = "POST")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestBody VerificationRequest request, HttpServletRequest httpRequest) {
    	authService.verifyEmail(request);
        return ResponseEntity.ok(new ApiResponse("Email verified successfully", HttpStatus.OK));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refresh(@RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
    	Map<String, String> tokens = authService.refreshSession(request);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK, tokens));
    }
    
    @PostMapping("/request-password-reset")
    @LogEndpoint(methodType = "POST")
    public ResponseEntity<ApiResponse> requestPasswordReset(@RequestBody EmailRequest request, HttpServletRequest httpRequest) {
        authService.requestPasswordReset(request);
        ApiResponse response = new ApiResponse("Password reset code sent. Please check your email.", HttpStatus.CREATED);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @PostMapping("/verify-password-reset")
    @LogEndpoint(methodType = "POST")
    public ResponseEntity<ApiResponse> verifyPasswordReset(@RequestBody VerificationRequest request, HttpServletRequest httpRequest) {
    	Map<String, String> token = authService.verifyPasswordReset(request);
    	return ResponseEntity.ok(new ApiResponse(HttpStatus.OK, token));
    }
    
    @PostMapping("/reset-password")
    @LogEndpoint(methodType = "POST")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody PasswordResetRequest request, HttpServletRequest httpRequest) {
        authService.authorizePasswordReset(request);
        return ResponseEntity.ok(new ApiResponse("Password reset successfully", HttpStatus.OK));
    }
}
