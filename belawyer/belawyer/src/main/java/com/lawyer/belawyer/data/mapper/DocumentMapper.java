package com.lawyer.belawyer.data.mapper;

import com.lawyer.belawyer.data.dto.DocumentDto;
import com.lawyer.belawyer.data.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    public DocumentDto toDto(Document document){
        if(document==null){
            return null;
        }
        return new DocumentDto();
    }
    public Document toEntity(DocumentDto dto){
        if(dto==null){
            return null;
        }
        Document document = new Document();
        return document;
    }
}
