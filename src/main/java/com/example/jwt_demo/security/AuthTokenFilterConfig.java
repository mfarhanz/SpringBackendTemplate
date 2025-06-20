package com.example.jwt_demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.jwt_demo.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AuthTokenFilterConfig {
	
	private final AuthEntryPointJwt authEntryPoint;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtils;
    
    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter(authEntryPoint, userDetailsService, jwtUtils);
    }
}
