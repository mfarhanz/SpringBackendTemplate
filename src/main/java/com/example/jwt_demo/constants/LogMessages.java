package com.example.jwt_demo.constants;

public enum LogMessages {
	USER_EXISTS("User account already exists"),
	USER_CREATED("User account created"),
	USER_DELETED("User account deleted"),
	USER_NOT_FOUND("User account was not found"),
	USER_NOT_VERIFIED("User account not verified"),
	USER_TOKENS_UPDATED("Tokens created/updated for user"),
	USER_TOKEN_DELETED("Token revoked for user"),
	USER_TOKEN_NOT_FOUND("Token was not found for user"),
	USER_TOKEN_EXPIRED("Token has expired and cannot be used for user"),
	UNEXPECTED_TOKEN("Received unexpected token type"),
	TOKEN_MISMATCH("Received token did not match token in repository"),
	TOKEN_INVALID("Received expired or revoked token"),
	BAD_CREDENTIALS("User credentials could not be authenticated for user");	
	
	private final String message;
	
	LogMessages(String message) {
        this.message = message;
    }
	
	@Override
    public String toString() {
        return message;
    }
}
