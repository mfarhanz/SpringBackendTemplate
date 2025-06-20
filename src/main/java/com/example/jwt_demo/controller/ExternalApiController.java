package com.example.jwt_demo.controller;

import com.example.jwt_demo.service.ExternalApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external")
public class ExternalApiController {

    private final ExternalApiService apiService;

    public ExternalApiController(ExternalApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/{resource}")
    public ResponseEntity<String> getResource(@PathVariable("resource") String resource) {
        String response = apiService.fetchResource(resource);
        return ResponseEntity.ok(response);
    }
}

