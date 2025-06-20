package com.example.jwt_demo.service;

import com.example.jwt_demo.model.Collection;
import com.example.jwt_demo.model.Document;
import com.example.jwt_demo.repository.DocumentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

	private final DocumentRepository documentRepository;
//    private final CollectionRepository collectionRepository;    
    private final CollectionService collectionService;
        
    public int getNextCollectionScopedId(Integer userScopedCollectionId, Long userId) {
    	return documentRepository
                .findMaxCollectionScopedIdByCollectionUserScopedIdAndCollectionUserId(userScopedCollectionId, userId)
                .orElse(0) + 1;
    }
    
//    private Collection getUserOwnedCollection(Integer userScopedId, Long userId) {		// is this needed?
////        Collection collection = collectionRepository.findByUserIdAndUserScopedId(userId, userScopedId)
////                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));
//        Collection collection = collectionService.getCollection(userId, userScopedId);
//        if (!collection.getUser().getId().equals(userId)) {		// redundant check
//            throw new SecurityException();
//        }
//        return collection;
//    }

    public List<Document> getCollectionDocuments(Integer userScopedCollectionId, Long userId) {
    	// This will throw EntityNotFoundException if collection doesn't exist
        collectionService.getCollection(userId, userScopedCollectionId);
        return documentRepository.findByCollectionUserScopedIdAndCollectionUserId(userScopedCollectionId, userId);
    }

    public Document getDocument(Integer userScopedCollectionId, Integer collectionScopedDocumentId, Long userId) {
    	return documentRepository
                .findByCollectionUserScopedIdAndCollectionUserIdAndCollectionScopedId(userScopedCollectionId, userId, collectionScopedDocumentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));
    }
    
    public Document addDocument(Integer userScopedCollectionId, Long userId, Document doc) {
//        Collection collection = getUserOwnedCollection(userScopedCollectionId, userId);		// getUserOwnedCollection == collectionService.getCollection ?
        Collection collection = collectionService.getCollection(userId, userScopedCollectionId);
        doc.setCollection(collection);
        doc.setCollectionScopedId(getNextCollectionScopedId(userScopedCollectionId, userId));
        return documentRepository.save(doc);
    }
    
    public Document updateDocument(Integer userScopedCollectionId, Integer collectionScopedDocumentId,  Long userId, Document updated) {
        Document existing = getDocument(userScopedCollectionId, collectionScopedDocumentId, userId);
        existing.setTitle(updated.getTitle());
        existing.setContent(updated.getContent());
        return documentRepository.save(existing);
    }

    public void deleteDocument(Integer userScopedCollectionId, Integer collectionScopedDocumentId, Long userId) {
        Document doc = getDocument(userScopedCollectionId, collectionScopedDocumentId, userId);
        documentRepository.delete(doc);
    }
}
