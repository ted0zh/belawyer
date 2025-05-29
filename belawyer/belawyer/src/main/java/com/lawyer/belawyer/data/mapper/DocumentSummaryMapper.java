package com.lawyer.belawyer.data.mapper;

import com.lawyer.belawyer.data.dto.DocumentSummaryDto;
import com.lawyer.belawyer.data.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentSummaryMapper {
    public DocumentSummaryDto toDto(Document document){
        if(document==null){
            return null;
        }
        return new DocumentSummaryDto(
                document.getId(),
                document.getSummary(),
                document.getName()

        );
    }
}
