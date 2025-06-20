package com.example.jwt_demo.exception;

public class InvalidDataException extends RuntimeException {
	private static final long serialVersionUID = 1L;	// suppress warning

	public InvalidDataException(String message) {
        super(message);
    }
}
