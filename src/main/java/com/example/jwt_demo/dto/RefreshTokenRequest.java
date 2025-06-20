package com.example.jwt_demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenRequest {
	private String refreshToken;
	private String deviceId;
}
