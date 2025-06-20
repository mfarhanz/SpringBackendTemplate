package com.example.jwt_demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class PasswordResetRequest {
	private String sessionToken;
    private String password;
}
