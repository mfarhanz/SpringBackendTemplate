package com.example.jwt_demo.dto;

import com.example.jwt_demo.model.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class DocumentDTO {
    private Integer id;
    private String title;
    private String content;

    public DocumentDTO(Document doc) {
        this.setId(doc.getCollectionScopedId());
        this.setTitle(doc.getTitle());
        this.setContent(doc.getContent());
    }
}
