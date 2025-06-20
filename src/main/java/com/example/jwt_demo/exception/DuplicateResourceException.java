package com.example.jwt_demo.exception;

public class DuplicateResourceException extends RuntimeException {
	private static final long serialVersionUID = 1L;	// suppress warning
	
    public DuplicateResourceException(String message) {
        super(message);
    }
}
