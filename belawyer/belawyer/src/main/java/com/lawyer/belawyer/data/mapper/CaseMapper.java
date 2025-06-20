package com.lawyer.belawyer.data.mapper;

import com.lawyer.belawyer.data.dto.CaseDto;
import com.lawyer.belawyer.data.dto.CaseResponseDto;
import com.lawyer.belawyer.data.entity.Case;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CaseMapper {

   public CaseDto toDto(Case legalCase){
       if(legalCase==null){
           return null;
       }
       return new CaseDto(
               legalCase.getStatus(),
               legalCase.getDescription(),
               legalCase.getTitle(),
               legalCase.getInstitution()

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
        legalCase.setInstitution(dto.getInstitution());

        return legalCase;
    }


    public CaseResponseDto toResponseDto(Case legalCase) {
        if (legalCase == null) {
            return null;
        }
        CaseResponseDto dto = new CaseResponseDto();
        dto.setId(legalCase.getId());
        dto.setStatus(legalCase.getStatus());
        dto.setDescription(legalCase.getDescription());
        dto.setTitle(legalCase.getTitle());
        dto.setInstitution(legalCase.getInstitution());
        dto.setUsername(
                legalCase.getUser() != null ? legalCase.getUser().getUsername() : null
        );
        return dto;
    }

    public List<CaseResponseDto> toResponseDtoList(List<Case> cases) {
        if (cases == null) {
            return null;
        }

        List<CaseResponseDto> list = new ArrayList<>(cases.size());
        for (Case legalCase : cases) {
            list.add(toResponseDto(legalCase));
        }

        return list;
    }


}
