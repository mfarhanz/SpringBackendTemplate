package com.example.jwt_demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
	
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
    	int status = HttpServletResponse.SC_UNAUTHORIZED;
        // Extract the cause or look up a request attribute
    	Throwable originalException = (Throwable) request.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        String message = originalException != null
            ? originalException.getMessage()
            : authException.getMessage();
        String json = String.format(
            "{\"error\": \"%s\", \"status\": %d, \"message\": \"%s\"}",
            "Unauthorized",
            status,
            message.replace("\\", "\\\\")
            		.replace("\"", "\\\"")
            		.replace("\n", "\\n")
            		.replace("\r", "\\r")
        );
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(json);
    }
}
