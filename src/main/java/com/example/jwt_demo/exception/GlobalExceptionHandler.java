package com.example.jwt_demo.exception;

import com.example.jwt_demo.dto.ApiError;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;

import org.hibernate.PropertyValueException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleException(Exception ex) throws Exception {
		String error, message;
		HttpStatus status; 
		if (ex instanceof EntityNotFoundException) {
			error = "Not Found";
			message = "Entity not found";
			status = HttpStatus.NOT_FOUND;
		} 
		else if (ex instanceof SecurityException) {
			error = "Unauthorized";
			message = "Unauthorized user access";
			status = HttpStatus.UNAUTHORIZED;
		}  
		else if (ex instanceof AccessDeniedException) {
			error = "Forbidden";
			message = "Access denied";
			status = HttpStatus.FORBIDDEN;
		}
		else if (ex instanceof InvalidDataException) {
			error = "Bad Request";
			message = "Invalid data";
			status = HttpStatus.BAD_REQUEST;
		}
		else if (ex instanceof BadCredentialsException) {
		    error = "Unauthorized";
		    message = "Bad credentials. Invalid username, email or password.";
		    status = HttpStatus.UNAUTHORIZED;
		}
		else if (ex instanceof DuplicateResourceException) {
			error = "Conflict";
			message = "Resource already exists";
			status = HttpStatus.CONFLICT;
		} 
		else if (ex instanceof IllegalStateException) {
			error = "Bad Request";
			message = "Illegal request";
			status = HttpStatus.BAD_REQUEST;
		}
		else if (ex instanceof IllegalArgumentException) {
		    error = "Bad Request";
		    message = "The request has invalid or missing parameters";
		    status = HttpStatus.BAD_REQUEST;
		}
		else if (ex instanceof SignatureException) {
		    error = "Unauthorized";
		    message = "Invalid JWT signature";
		    status = HttpStatus.UNAUTHORIZED;
		}
		else if (ex instanceof MalformedJwtException) {
		    error = "Unauthorized";
		    message = "Invalid JWT token";
		    status = HttpStatus.UNAUTHORIZED;
		}
		else if (ex instanceof UnsupportedJwtException) {
		    error = "Unauthorized";
		    message = "JWT token is unsupported";
		    status = HttpStatus.UNAUTHORIZED;
		}
		else if (ex instanceof PropertyValueException) {
		    error = "Bad Request";
		    message = "Required property is missing or null";
		    status = HttpStatus.BAD_REQUEST;
		}
		else if (ex instanceof HttpMessageNotReadableException) {
		    error = "Bad Request";
		    message = "Could not parse JSON request";
		    status = HttpStatus.BAD_REQUEST;
		}
		else if (ex instanceof DataIntegrityViolationException) {
		    error = "Conflict";
		    message = "A data integrity violation occurred";
		    status = HttpStatus.CONFLICT;
		}
		else {
			error = "Internal Server Error";
			message = "Something went wrong. Please try again later.";
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		String exMessage = ex.getMessage();
		ApiError apiError = new ApiError(
    	        error,
    	        status.value(),
    	        (exMessage != null && !exMessage.isEmpty()) ? exMessage : message
    	    );
		return ResponseEntity.status(apiError.getStatus()).body(apiError);
	}
}
