package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.Document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
	
    List<Document> findByCollectionUserScopedIdAndCollectionUserId(Integer userScopedCollectionId, Long userId);
    
    Optional<Document> findByCollectionUserScopedIdAndCollectionUserIdAndCollectionScopedId(
    	    Integer userScopedCollectionId,
    	    Long userId,
    	    Integer collectionScopedDocumentId);

    @Query("SELECT MAX(d.collectionScopedId) FROM Document d WHERE d.collection.userScopedId = :userScopedCollectionId AND d.collection.user.id = :userId")
    Optional<Integer> findMaxCollectionScopedIdByCollectionUserScopedIdAndCollectionUserId(
    		@Param("userScopedCollectionId") Integer userScopedCollectionId,
    		@Param("userId") Long userId);
}
