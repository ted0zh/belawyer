package com.lawyer.belawyer.service;

import com.lawyer.belawyer.data.dto.CaseDto;
import com.lawyer.belawyer.data.entity.Case;

import java.util.List;
import java.util.Optional;

public interface CaseService {
    List<Case> getAllCases();


    Optional<Case> getCaseById(Long id);

    Case saveCase(CaseDto dto);

    void attachCase(Long caseId,String username);

    void deleteCase(Long id) ;


}
