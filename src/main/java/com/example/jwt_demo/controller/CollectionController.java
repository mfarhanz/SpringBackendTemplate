package com.example.jwt_demo.controller;

import com.example.jwt_demo.dto.CollectionDTO;
import com.example.jwt_demo.model.Collection;
import com.example.jwt_demo.repository.UserRepository;
import com.example.jwt_demo.service.CollectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {
	
	private final CollectionService collectionService;
    private final UserRepository userRepository;
    
    private Long extractUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getPassword()).getId();
    }
    
    @GetMapping
    public ResponseEntity<List<CollectionDTO>> getCollections(@AuthenticationPrincipal UserDetails userDetails) {
        List<CollectionDTO> collections = collectionService.getUserCollections(extractUserId(userDetails))
                .stream().map(CollectionDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(collections);
    }
        
    // READ: Get a collection by userScopedId
    @GetMapping("/{id}")
    public ResponseEntity<CollectionDTO> getCollectionById(@PathVariable("id") Integer id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Collection collection = collectionService.getCollection(extractUserId(userDetails), id);
        return ResponseEntity.ok(new CollectionDTO(collection));
    }

    @PostMapping
    public ResponseEntity<CollectionDTO> createCollection(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestBody Collection collection) {
        Collection saved = collectionService.createCollection(extractUserId(userDetails), collection);
        return ResponseEntity.ok(new CollectionDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable("id") Integer id,
                                              @AuthenticationPrincipal UserDetails userDetails) {
    	collectionService.deleteCollection(extractUserId(userDetails), id);
    	return ResponseEntity.noContent().build();
    }
}
