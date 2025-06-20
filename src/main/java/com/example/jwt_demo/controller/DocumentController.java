package com.example.jwt_demo.controller;

import com.example.jwt_demo.dto.DocumentDTO;
import com.example.jwt_demo.model.Document;
import com.example.jwt_demo.repository.UserRepository;
import com.example.jwt_demo.service.DocumentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/collections/{collectionId}/documents")
@RequiredArgsConstructor
public class DocumentController {

	private final DocumentService documentService;
    private final UserRepository userRepository;
    
    private Long extractUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).getId();
    }
    
    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getDocuments(@PathVariable("collectionId") Integer collectionId,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        List<DocumentDTO> docs = documentService.getCollectionDocuments(collectionId, extractUserId(userDetails))
                .stream().map(DocumentDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(docs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable("id") Integer id,
                                             @PathVariable("collectionId") Integer collectionId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        Document doc = documentService.getDocument(collectionId, id, extractUserId(userDetails));
        return ResponseEntity.ok(new DocumentDTO(doc));
    }

    @PostMapping
    public ResponseEntity<DocumentDTO> addDocument(@PathVariable("collectionId") Integer collectionId,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody Document doc) {
        Document saved = documentService.addDocument(collectionId, extractUserId(userDetails), doc);
        return ResponseEntity.ok(new DocumentDTO(saved));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(@PathVariable("id") Integer id,
                                            @PathVariable("collectionId") Integer collectionId,
                                            @RequestBody Document updated,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        Document doc = documentService.updateDocument(collectionId, id, extractUserId(userDetails), updated);
        return ResponseEntity.ok(new DocumentDTO(doc));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable("id") Integer id,
                                            @PathVariable("collectionId") Integer collectionId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
    	documentService.deleteDocument(collectionId, id, extractUserId(userDetails));
    	return ResponseEntity.noContent().build();
    }
}
