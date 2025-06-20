package com.example.jwt_demo.service;

import com.example.jwt_demo.model.Collection;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.CollectionRepository;
import com.example.jwt_demo.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionService {
	
	private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    
    public int getNextUserScopedId(Long userId) {
        return collectionRepository.findMaxUserScopedIdByUserId(userId).orElse(0) + 1;
    }

    public List<Collection> getUserCollections(Long userId) {
        return collectionRepository.findByUserId(userId);
    }
    
    public Collection getCollection(Long userId, Integer userScopedId) {
        return collectionRepository.findByUserIdAndUserScopedId(userId, userScopedId)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));
    }

    public Collection createCollection(Long userId, Collection collection) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        collection.setUser(user);
        collection.setUserScopedId(getNextUserScopedId(userId));
        return collectionRepository.save(collection);
    }
    
    public Collection updateCollection(Long userId, Integer userScopedId, Collection updated) {
        Collection existingCollection = getCollection(userId, userScopedId);
        existingCollection.setName(updated.getName());
        existingCollection.setDescription(updated.getDescription());
        return collectionRepository.save(existingCollection);
    }

    public void deleteCollection(Long userId, Integer userScopedId) {
    	Collection collection = getCollection(userId, userScopedId);
        collectionRepository.delete(collection);
    }
}
