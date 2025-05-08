package com.lawyer.belawyer.data.mapper;

import com.lawyer.belawyer.data.dto.CaseDto;
import com.lawyer.belawyer.data.entity.Case;
import org.springframework.stereotype.Component;

@Component
public class CaseMapper {

   public CaseDto toDto(Case legalCase){
       if(legalCase==null){
           return null;
       }
       return new CaseDto(
               legalCase.getStatus(),
               legalCase.getDescription(),
               legalCase.getTitle()

       );
   }

    public Case toEntity(CaseDto dto){
        if (dto == null) {
            return null;
        }

        Case legalCase = new Case();
        legalCase.setDescription(dto.getDescription());
        legalCase.setTitle(dto.getTitle());
        legalCase.setStatus(dto.getStatus());

        return legalCase;
    }
}
