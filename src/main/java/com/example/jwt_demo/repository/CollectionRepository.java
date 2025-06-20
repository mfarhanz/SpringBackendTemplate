package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
	
	List<Collection> findByUserId(Long userId);
	
	Optional<Collection> findByUserIdAndUserScopedId(Long userId, Integer userScopedId);
    
    @Query("SELECT MAX(c.userScopedId) FROM Collection c WHERE c.user.id = :userId")
    Optional<Integer> findMaxUserScopedIdByUserId(@Param("userId") Long userId);
}
