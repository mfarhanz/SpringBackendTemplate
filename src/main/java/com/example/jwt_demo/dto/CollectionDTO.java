package com.example.jwt_demo.dto;

import com.example.jwt_demo.model.Collection;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CollectionDTO {
	private Integer id;
    private String name;
    private String description;

    public CollectionDTO(Collection collection) {
    	this.setId(collection.getUserScopedId());
        this.setName(collection.getName());
        this.setDescription(collection.getDescription());
    }
}
