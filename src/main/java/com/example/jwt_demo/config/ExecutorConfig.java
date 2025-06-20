package com.example.jwt_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {
	
	@Bean(name = "emailExecutor")
    public ExecutorService emailExecutorService() {
        // Fixed thread pool with 10 threads
        return Executors.newFixedThreadPool(10);
    }
}
