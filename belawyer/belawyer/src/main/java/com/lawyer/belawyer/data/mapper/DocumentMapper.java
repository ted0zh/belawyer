package com.lawyer.belawyer.data.mapper;

import com.lawyer.belawyer.data.dto.DocumentDto;
import com.lawyer.belawyer.data.entity.Document;

import java.sql.Timestamp;
import java.time.Instant;

public class DocumentMapper {
    public DocumentDto toDto(Document document){
        if(document==null){
            return null;
        }
        return new DocumentDto(document.getCategory(),document.getUploadedAt());
    }
    public Document toEntity(DocumentDto dto){
        if(dto==null){
            return null;
        }
        Document document = new Document();
        document.setCategory(dto.getCategory());
        document.setUploadedAt(dto.getUploadedAt() != null ? dto.getUploadedAt() : Timestamp.from(Instant.now()));
        document.setFilePath(dto.getFilePath());
        return document;
    }
}
