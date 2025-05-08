package com.lawyer.belawyer.data.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDto {
    private String filePath;
    private String category;
    private Timestamp uploadedAt;

    public DocumentDto(String category, Timestamp uploadedAt) {
        this.category=category;
        this.uploadedAt=uploadedAt;
    }
}
