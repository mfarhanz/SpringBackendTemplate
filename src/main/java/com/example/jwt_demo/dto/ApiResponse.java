package com.example.jwt_demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse {
    private String message;
    private int status;
    private Object data;

    public ApiResponse(String message, HttpStatus status) {
        this.status = status.value();
        this.message = message;
    }
    
    public ApiResponse(HttpStatus status, Object data) {
        this.status = status.value();
        this.data = data;
    }
    
    public ApiResponse(String message, HttpStatus status, Object data) {
    	this.status = status.value();
        this.message = message;
        this.data = data;
    }
}
