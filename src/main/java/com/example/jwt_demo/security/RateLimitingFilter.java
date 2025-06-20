package com.example.jwt_demo.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.jwt_demo.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {
	
	private final Map<String, Bucket> buckets;
	
	@Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        Bucket bucket = getBucket(ip, path);
        if (bucket == null || bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
        	ApiError error = new ApiError("Too many requests", HttpStatus.TOO_MANY_REQUESTS.value(), getRateLimitErrorMessage(path));
        	String json = new ObjectMapper().writeValueAsString(error);
        	
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(json);
        }
    }
	
	private Bucket getBucket(String ip, String path) {
        Bandwidth limit;
        
        switch (path) {
	        case "/api/auth/signin":
	            limit = Bandwidth.builder()
	                    .capacity(5)
	                    .refillGreedy(5, Duration.ofMinutes(1))
	                    .build();
	            break;
	        case "/api/auth/signup":
	            limit = Bandwidth.builder()
	                    .capacity(5)
	                    .refillGreedy(5, Duration.ofHours(1))
	                    .build();
	            break;
	        case "/api/auth/request-email-verify":
	            limit = Bandwidth.builder()
	                    .capacity(3)
	                    .refillGreedy(3, Duration.ofMinutes(10))
	                    .build();
	            break;
	        case "/api/auth/verify-email":
	            limit = Bandwidth.builder()
	                    .capacity(5)
	                    .refillGreedy(5, Duration.ofMinutes(10))
	                    .build();
	            break;
	        case "/api/auth/refresh":
	            limit = Bandwidth.builder()
	                    .capacity(30)
	                    .refillGreedy(30, Duration.ofMinutes(1))
	                    .build();
	            break;
	        case "/api/auth/signout":
	            limit = Bandwidth.builder()
	                    .capacity(10)
	                    .refillGreedy(10, Duration.ofMinutes(5))
	                    .build();
	            break;
	        default:
	            return null; // No limit on other endpoints
	    }

        String key = ip + ":" + path;
        return buckets.computeIfAbsent(key, k -> Bucket.builder().addLimit(limit).build());
    }
	
	private String getRateLimitErrorMessage(String path) {
	    return switch (path) {
	        case "/api/auth/signin" -> "Too many login attempts. Please wait a bit before trying again.";
	        case "/api/auth/signup" -> "There were too many requests made in a short amount of time. Please try again later.";
	        case "/api/auth/request-email-verify" -> "Too many verification codes have been sent. Enter the last code you received or try again later.";
	        case "/api/auth/verify-email" -> "You have reached the limit for verification attempts. Please sign-up again or try again later.";
	        case "/api/auth/refresh" -> "No updates available at the moment, you're all caught up for now ";
	        case "/api/auth/signout" -> "Too many attempts were made to log out. You may already be logged out, try restarting the app.";
	        default -> "You have sent too many requests in a short amount of time. Please try again later.";
	    };
	}
}
