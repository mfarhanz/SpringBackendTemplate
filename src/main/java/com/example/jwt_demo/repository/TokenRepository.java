package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.User;
import com.example.jwt_demo.model.Token;
import com.example.jwt_demo.model.TokenType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
	
    Optional<Token> findByUserAndType(User user, TokenType type);
    Optional<Token> findByTokenAndType(String token, TokenType type);
    Optional<Token> findByUserAndDeviceIdIsNullAndType(User user, TokenType type);
    Optional<Token> findByUserAndDeviceIdAndType(User user, String deviceId, TokenType type);
    void deleteByUserAndType(User user, TokenType type);
    void deleteByUserAndDeviceIdIsNullAndType(User user, TokenType type);
    void deleteByUserAndDeviceIdAndType(User user, String deviceId, TokenType type);
}
