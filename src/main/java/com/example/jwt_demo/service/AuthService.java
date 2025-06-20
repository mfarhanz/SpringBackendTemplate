package com.example.jwt_demo.service;

import com.example.jwt_demo.dto.VerificationRequest;
import com.example.jwt_demo.dto.EmailRequest;
import com.example.jwt_demo.constants.LogMessages;
import com.example.jwt_demo.dto.AuthRequest;
import com.example.jwt_demo.dto.PasswordResetRequest;
import com.example.jwt_demo.dto.RefreshTokenRequest;
import com.example.jwt_demo.dto.UserDTO;
import com.example.jwt_demo.exception.InvalidDataException;
import com.example.jwt_demo.model.Role;
import com.example.jwt_demo.model.Token;
import com.example.jwt_demo.model.TokenType;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.TokenRepository;
import com.example.jwt_demo.repository.UserRepository;
import com.example.jwt_demo.security.JwtUtil;
import com.example.jwt_demo.service.AuthService;
import com.example.jwt_demo.utils.CodeGenerator;
import com.example.jwt_demo.utils.ValidationUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtils;
    private final EmailService emailService;
    
    @Transactional
    public Map<String, Object> authenticateUser(AuthRequest request) {
        String usernameOrEmail = request.getUsername();  // This field could be either username or email
        String password = request.getPassword();
        if (password == null || password.isEmpty()) {
        	throw new InvalidDataException("Password is required");
        }
        if (usernameOrEmail == null || usernameOrEmail.isEmpty()) {
        	throw new InvalidDataException("Username or email is required");
        }
        // Find user by either username or email
        User existingUser = ValidationUtils.isValidEmail(usernameOrEmail)
                ? userRepository.findByEmail(usernameOrEmail)
                : userRepository.findByUsername(usernameOrEmail);
        if (existingUser == null) {
        	log.warn("{} with Username: {}", LogMessages.USER_NOT_FOUND, usernameOrEmail);
        	throw new BadCredentialsException("");
        }
        if (!existingUser.isEmailVerified()) {
        	log.warn("{} with ID: {}", LogMessages.USER_NOT_VERIFIED, existingUser.getId());
        	throw new SecurityException("Email not verified. Please check your email or request a new verification code.");
        }
        String deviceId, username = null;
        try {
        	Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(usernameOrEmail, password));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        } catch (BadCredentialsException e) {
        	log.warn("{} with Username: {}", LogMessages.BAD_CREDENTIALS, usernameOrEmail);
        	throw new BadCredentialsException("The password you entered is incorrect. Please try again.");
        }
        deviceId = request.getDeviceId();
        // Update user's last login time
        existingUser.setLastLogin(LocalDateTime.now());
        saveUser(existingUser);
        // Generate tokens
        String accessToken = jwtUtils.generateAccessToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);
        // Save refresh token in DB
        Token token = new Token();
        token.setToken(refreshToken);
        token.setExpiry(LocalDateTime.now().plusDays(7));
        token.setType(TokenType.REFRESH_TOKEN);
        token.setDeviceId(deviceId);
        token.setUser(existingUser);
        
        if (deviceId != null && !deviceId.isBlank()) {
            deleteTokenByUserAndDeviceIdAndType(existingUser, deviceId, TokenType.REFRESH_TOKEN);
        } else {
            // Delete tokens with with no provided deviceId
            deleteTokenByUserAndTypeWithNoDeviceId(existingUser, TokenType.REFRESH_TOKEN);
        }
        
        tokenRepository.flush();		// if this isnt called, a transactional error is thrown, and sign in cannot be completed
        saveToken(token, existingUser);
        // Return both tokens
        Map<String, Object> response = new HashMap<>();
        response.put("access-token", accessToken);
        response.put("refresh-token", refreshToken);
        response.put("user", new UserDTO(existingUser));
        return response ;
    }
    
    @Transactional
    public void logoutUser(AuthRequest request) {
    	String usernameOrEmail = request.getUsername();
    	String deviceId = request.getDeviceId();
    	// Find user by either username or email
        User existingUser = ValidationUtils.isValidEmail(usernameOrEmail)
                ? userRepository.findByEmail(usernameOrEmail)
                : userRepository.findByUsername(usernameOrEmail);
        if (existingUser == null) {
        	log.warn("{} with Username: {}", LogMessages.USER_NOT_FOUND, usernameOrEmail);
        	throw new BadCredentialsException("There was an error during the logout process, please restart the application.");
        }
        authManager.authenticate(new UsernamePasswordAuthenticationToken(usernameOrEmail, request.getPassword()));		// needed or no?
        // Delete the refresh token associated with this device (or null device)
        if (deviceId != null && !deviceId.isBlank()) {
            deleteTokenByUserAndDeviceIdAndType(existingUser, deviceId, TokenType.REFRESH_TOKEN);
        } else {
            deleteTokenByUserAndTypeWithNoDeviceId(existingUser, TokenType.REFRESH_TOKEN);
        }
    }
    
    public void registerUser(User user) {
    	String defaultMessage = "Unable to register user with these credentials";
    	String email = user.getEmail();
    	String username = user.getUsername();
    	// Reject if password is null or empty
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
        	throw new InvalidDataException("Password is required");
        }
        // If email is provided, validate format and check uniqueness
        if (email != null && !email.isEmpty()) {
            if (!ValidationUtils.isValidEmail(email)) {
            	throw new InvalidDataException("Invalid email format");
            }
            if (userRepository.existsByEmail(email)) {
            	log.warn("{} with Email Address: {}", LogMessages.USER_EXISTS, email);
            	throw new BadCredentialsException(defaultMessage);
            }
        }
        else {
        	throw new InvalidDataException("Email must be provided");
        }
        // If username is missing but email is present and valid, set username = email
        if ((username == null || username.isEmpty())) {
            user.setUsername(email);  // Use email as username
        }
        // Check if username is taken
        if (userRepository.existsByUsername(username)) {
        	log.warn("{} with Username: {}", LogMessages.USER_EXISTS, username);
        	throw new BadCredentialsException(defaultMessage);
        }
        // Register user (not finalized until email verified)
        Set<Role> roles = Set.of(Role.ROLE_USER);
        User newUser = new User(
                username,
                email,
                encoder.encode(user.getPassword()),
                roles
        );
        newUser.setEmailVerified(false);
        saveUser(newUser);
    }
    
    public void requestEmailVerification(EmailRequest request) {
    	String email = request.getEmail();
    	if (!ValidationUtils.isValidEmail(email)) {
    		throw new InvalidDataException("Invalid email format");
    	}
    	User user = userRepository.findByEmail(email);
    	if (user == null) {
    		log.warn("{} with Email Address: {}", LogMessages.USER_NOT_FOUND, email);
        	throw new RuntimeException("There was an error processing your request. Please restart the app or try again later.");
    	}
        Optional<Token> existingTokenOpt = tokenRepository.findByUserAndType(user, TokenType.EMAIL_VERIFICATION);
        if (existingTokenOpt.isPresent() && existingTokenOpt.get().getExpiry().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("A reset code has already been sent and is still valid");
        }
        // Delete old verification tokens
        deleteTokenByUserAndType(user, TokenType.EMAIL_VERIFICATION);
    	// Create verification code
        String verificationCode = CodeGenerator.generate6DigitCode();
        Token verificationToken = new Token();
        verificationToken.setToken(verificationCode);
        verificationToken.setExpiry(LocalDateTime.now().plusMinutes(10));
        verificationToken.setAttempts(0); 		// only for verification purposes
        verificationToken.setType(TokenType.EMAIL_VERIFICATION);
        verificationToken.setUser(user);
        saveToken(verificationToken, user);
        // Send email with code
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
    }
    
    public void verifyEmail(VerificationRequest request) {
    	String email = request.getEmail();
    	User user = userRepository.findByEmail(email);
    	if (user == null) {
    		log.warn("{} with Email Address: {}", LogMessages.USER_NOT_FOUND, email);
			throw new RuntimeException();
    	}
        // Check if already verified
        if (user.isEmailVerified()) {
        	throw new IllegalStateException("Email already verified");
        }
        
        Token token = tokenRepository.findByUserAndType(user, TokenType.EMAIL_VERIFICATION)
        	    .orElseThrow(() -> {
        	    	log.warn("{} with ID: {} , TokenType: {}", LogMessages.USER_TOKEN_NOT_FOUND, user.getId(), TokenType.EMAIL_VERIFICATION);
        	    	return new EntityNotFoundException("There was an error verifying your account. Please request another verification code.");
        	    });
        // Check if code has expired
        if (token.getExpiry().isBefore(LocalDateTime.now())) {
        	log.warn("{} with ID: {} , Token ID: {}", LogMessages.USER_TOKEN_EXPIRED, user.getId(), token.getId());
        	deleteToken(token, user);
//        	deleteUser(user);			don't delete user in this case?
            throw new AccessDeniedException("Verification code expired. Please request another verification code.");
        }
        // Check if too many attempts have been made
        if (token.getAttempts() != null && token.getAttempts() >= 4) {
        	deleteToken(token, user);
            deleteUser(user);
            throw new AccessDeniedException("Too many verification codes have been sent. Please sign up again.");
        }
        // If code matches, forget token, finish registration
        if (request.getCode().equals(token.getToken())) {
            user.setEmailVerified(true);
            saveUser(user);
            deleteToken(token, user);		// Remove token after success
            return;
        }
        // ...otherwise increment failed attempts for token
        token.setAttempts(token.getAttempts() == null ? 1 : token.getAttempts() + 1);
        tokenRepository.save(token);
        int attempts = 5 - token.getAttempts();
        throw new SecurityException("Incorrect code. You have " + attempts + " more attempt" + (attempts == 1 ? "." : "s."));
    }
    
    public Map<String, String> refreshSession(RefreshTokenRequest request) {
    	String deviceId = request.getDeviceId();
    	//Validate refresh token
        String token  = request.getRefreshToken();
        String refreshError = "Unable to refresh at this time. Please sign-in again.";
        if (token == null || token.isEmpty()) {
            throw new InvalidDataException(refreshError);	// when refresh token missing
        }
        jwtUtils.validateJwtToken(token);
        if (!jwtUtils.getTokenType(token).equals("refresh")) {
        	log.error(LogMessages.UNEXPECTED_TOKEN);
        	throw new InvalidDataException(refreshError);	// when refresh token is the wrong type
        }
        // Extract username from refresh token
        String username = jwtUtils.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
        	log.warn("{} with Username: {}", LogMessages.USER_NOT_FOUND, username);
        	throw new RuntimeException("There was an error processing your request. Please sign-in again.");
        }
        Token savedToken;
        // When refresh token not recognized or revoked
        if (deviceId == null) {
            savedToken = tokenRepository.findByUserAndDeviceIdIsNullAndType(user, TokenType.REFRESH_TOKEN)
                    .orElseThrow(() -> new SecurityException(refreshError));
        } else {
            savedToken = tokenRepository.findByUserAndDeviceIdAndType(user, deviceId, TokenType.REFRESH_TOKEN)
                    .orElseThrow(() -> new SecurityException(refreshError));
        }
        // Check if the provided refresh token matches the one currently saved in repository
        if (!token.equals(savedToken.getToken())) {
        	log.warn(LogMessages.TOKEN_MISMATCH);
            throw new SecurityException(refreshError);
        }
        // If the current refresh token has expired, delete it and reject the session
        if (savedToken.getExpiry().isBefore(LocalDateTime.now())) {
        	deleteToken(savedToken, user);
            throw new SecurityException("User session expired. Please sign-in again.");
            
        }
        // Token is valid, so proceed with issuing new tokens
        String accessToken = jwtUtils.generateAccessToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);
        Token newToken = new Token();
        newToken.setToken(refreshToken);
        newToken.setExpiry(LocalDateTime.now().plusDays(7));
        newToken.setType(TokenType.REFRESH_TOKEN);
        newToken.setDeviceId(deviceId);
        newToken.setUser(user);
        // Invalidate old token (refresh token rotation)
        deleteToken(savedToken, user);
        saveToken(newToken, user);
        Map<String, String> sessionTokens = new HashMap<>();
        sessionTokens.put("access-token", accessToken);
        sessionTokens.put("refresh-token", refreshToken);
        return sessionTokens;
    }
    
    @Transactional
    public void requestPasswordReset(EmailRequest request) {
    	String email = request.getEmail();
    	if (!ValidationUtils.isValidEmail(email)) {
    		throw new InvalidDataException("Invalid email format");
    	}
        User user = userRepository.findByEmail(email);
        if (user == null) {
        	log.warn("{} with Email Address: {}", LogMessages.USER_NOT_FOUND, email);
        	throw new RuntimeException("There was an error processing your request. Please sign-in again or retry later.");
        }
        Optional<Token> existingTokenOpt = tokenRepository.findByUserAndType(user, TokenType.PASSWORD_RESET);
        if (existingTokenOpt.isPresent() && existingTokenOpt.get().getExpiry().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("A reset code has already been sent and is still valid");
        }
        // Delete old verification and session tokens
        deleteTokenByUserAndType(user, TokenType.PASSWORD_RESET);
        deleteTokenByUserAndType(user, TokenType.PASSWORD_RESET_SESSION);
        // Create verification code
        String verificationCode = CodeGenerator.generate6DigitCode();
        Token verificationToken = new Token();
        verificationToken.setUser(user);
        verificationToken.setToken(verificationCode);
        verificationToken.setAttempts(0);
        verificationToken.setType(TokenType.PASSWORD_RESET);
        verificationToken.setExpiry(LocalDateTime.now().plusMinutes(10));
        saveToken(verificationToken, user);
        // Send email with code
        emailService.sendPasswordResetEmail(user.getEmail(), verificationCode);
    }
    
    public Map<String, String> verifyPasswordReset(VerificationRequest request) {
    	String email = request.getEmail();
    	
        User user = userRepository.findByEmail(email);
        if (user == null) {
        	log.warn("{} with Email Address: {}", LogMessages.USER_NOT_FOUND, email);
        	throw new RuntimeException("Could not complete verification at this moment. Please sign-in again or retry later.");
        }
        
        Token resetToken = tokenRepository.findByUserAndType(user, TokenType.PASSWORD_RESET)
            .orElseThrow(() -> {
            	log.warn("{} with ID: {} , TokenType: {}", LogMessages.USER_TOKEN_NOT_FOUND, user.getId(), TokenType.PASSWORD_RESET);
            	return new EntityNotFoundException("Could not complete verification, this code has already been used earlier or has been revoked");
            });
        
        Map<String, String> token = new HashMap<>();
        
        Optional<Token> existingSession = tokenRepository.findByUserAndType(user, TokenType.PASSWORD_RESET_SESSION);
        if (existingSession.isPresent() && existingSession.get().getExpiry().isAfter(LocalDateTime.now())) {
            token.put("session-token", existingSession.get().getToken());
            return token;
        }

        if (resetToken.getExpiry().isBefore(LocalDateTime.now())) {
        	log.warn("{} with ID: {} , Token ID: {}", LogMessages.USER_TOKEN_EXPIRED, user.getId(), resetToken.getId());
            throw new InvalidDataException("Password reset code expired. Please request a new password reset code.");
        }
        
        if (resetToken.getAttempts() != null && resetToken.getAttempts() >= 4) {
        	deleteToken(resetToken, user);
            throw new AccessDeniedException("Too many failed attempts. Please request a new password reset code.");
        }
        
        // If code matches, forget token, finish registration
        if (request.getCode().equals(resetToken.getToken())) {
            // Generate temporary session token (valid for 15 min)
            String sessionCode = CodeGenerator.generateUUID();
            Token sessionToken = new Token();
            sessionToken.setToken(sessionCode);
            sessionToken.setUser(user);
            sessionToken.setType(TokenType.PASSWORD_RESET_SESSION);
            sessionToken.setExpiry(LocalDateTime.now().plusMinutes(15));
            saveToken(sessionToken, user);
            token.put("session-token", sessionCode);
            return token;
        }
        
        // ...otherwise increment failed attempts for token
        resetToken.setAttempts(resetToken.getAttempts() == null ? 1 : resetToken.getAttempts() + 1);
        saveToken(resetToken, user);
        int attempts = 5 - resetToken.getAttempts();
        throw new SecurityException("Incorrect code. You have " + attempts + " more attempt" + (attempts == 1 ? "." : "s."));
    }
    
    @Transactional
    public void authorizePasswordReset(PasswordResetRequest request) {
        Token sessionToken = tokenRepository.findByTokenAndType(request.getSessionToken(), TokenType.PASSWORD_RESET_SESSION)
            .orElseThrow(() -> {
            	log.warn("{} with TokenType: {}", LogMessages.USER_TOKEN_NOT_FOUND, TokenType.PASSWORD_RESET_SESSION);
            	throw new RuntimeException("Could not authorize password reset at this moment. Please refresh or try again later.");
            });

        if (sessionToken.getExpiry().isBefore(LocalDateTime.now())) {
        	log.warn("{} with TokenType: {}", LogMessages.USER_TOKEN_EXPIRED, TokenType.PASSWORD_RESET_SESSION);
            throw new SecurityException("Password reset session has expired");
        }

        User user = sessionToken.getUser();
        String password = request.getPassword();
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidDataException("Password cannot be empty");
        }
        user.setPassword(encoder.encode(password));
        saveUser(user);
        // Discard temporary session and verification tokens
        deleteTokenByUserAndType(user, TokenType.PASSWORD_RESET);
        deleteToken(sessionToken, user);
    }
    
    private void saveUser(User user) {
    	userRepository.save(user);
    	log.warn("{} with ID: {}", LogMessages.USER_CREATED, user.getId());
    }
    
    private void deleteUser(User user) {
    	userRepository.delete(user);
    	log.warn("{} with ID: {}", LogMessages.USER_DELETED, user.getId());
    }
    
    private void saveToken(Token token, User user) {
    	tokenRepository.save(token);
    	log.warn("{} with ID: {} , TokenType: {}", LogMessages.USER_TOKENS_UPDATED, user.getId(), token.getType());
    }
    
    private void deleteToken(Token token, User user) {
    	tokenRepository.delete(token);
    	log.warn("{} with ID: {} , Token ID: {}", LogMessages.USER_TOKEN_DELETED, user.getId(), token.getId());
    }
    
    private void deleteTokenByUserAndType(User user, TokenType type) {
    	tokenRepository.deleteByUserAndType(user, type);
    	log.warn("{} with ID: {} , TokenType: {}", LogMessages.USER_TOKEN_DELETED, user.getId(), type);
    }
    
    private void deleteTokenByUserAndDeviceIdAndType(User user, String deviceId, TokenType type) {
    	tokenRepository.deleteByUserAndDeviceIdAndType(user, deviceId, type);
    	log.warn("{} with ID: {} , TokenType: {} , Device ID: {}", LogMessages.USER_TOKEN_DELETED, user.getId(), type, deviceId);
    }
    
    private void deleteTokenByUserAndTypeWithNoDeviceId(User user, TokenType type) {
    	tokenRepository.deleteByUserAndDeviceIdIsNullAndType(user, type);
    	log.warn("{} with ID: {} , TokenType: {} , Device ID: ", LogMessages.USER_TOKEN_DELETED, user.getId(), type);
    }
}
