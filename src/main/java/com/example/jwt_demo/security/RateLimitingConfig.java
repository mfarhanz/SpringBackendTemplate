package com.example.jwt_demo.security;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bucket;

@Configuration
public class RateLimitingConfig {
	
	@Bean
    public ConcurrentHashMap<String, Bucket> buckets() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public RateLimitingFilter rateLimitingFilter(ConcurrentHashMap<String, Bucket> buckets) {
        return new RateLimitingFilter(buckets);
    }
}
