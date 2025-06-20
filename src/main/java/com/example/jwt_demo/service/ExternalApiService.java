package com.example.jwt_demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ExternalApiService {

    private final WebClient webClient;

    public ExternalApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String fetchResource(String resourceName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                		.scheme("https")
                        .host("fakerapi.it")
                        .path("/api/v2/{resource}")
                        .queryParam("_quantity", 1)
                        .build(resourceName))
                .retrieve()
                .bodyToMono(String.class)
                .block();  // Only use block() in non-reactive code
    }
}
