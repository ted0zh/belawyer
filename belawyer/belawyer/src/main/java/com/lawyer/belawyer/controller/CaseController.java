package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.CaseDto;
import com.lawyer.belawyer.data.dto.CaseResponseDto;
import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.mapper.CaseMapper;
import com.lawyer.belawyer.service.serviceImpl.CaseServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/case")
public class CaseController {
    private final CaseServiceImpl caseService;
    private final CaseMapper caseMapper;
    public CaseController(CaseServiceImpl caseService, CaseMapper caseMapper){
        this.caseService=caseService;
        this.caseMapper = caseMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<Case> create(@RequestBody CaseDto dto){
        return ResponseEntity.ok(caseService.saveCase(dto));
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<CaseResponseDto>> getAll(){
        return ResponseEntity.ok(caseService.getAllCases());
    }

    @GetMapping("/get/byInstitution")
    public ResponseEntity<List<CaseResponseDto>> get(@RequestParam String place){
        List<CaseResponseDto> cases = caseService.getCaseByInstitution(place);
        if(!cases.isEmpty()){
            return ResponseEntity.ok(cases);
        }else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/get/byLawyerName")
    public ResponseEntity<List<CaseResponseDto>> getByUsername(@RequestParam String username) {
        List<CaseResponseDto> assigned = caseService.getAllCasesByUsername(username);
        return ResponseEntity.ok(assigned);
    }

    @GetMapping("/get/byId")
    public ResponseEntity<CaseResponseDto> get(@RequestParam Long id) {
        Optional<CaseResponseDto> caseOpt = caseService.getCaseById(id);
        if (caseOpt.isPresent()) {
            return ResponseEntity.ok(caseOpt.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam Long id){
        Optional<CaseResponseDto> caseOpt = caseService.getCaseById(id);
        if(caseOpt.isPresent()){
            caseService.deleteCase(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<CaseResponseDto>> getUnassignedCases() {
        List<Case> unassigned = caseService.getAllUnassignedCases();
        List<CaseResponseDto> dtos = caseMapper.toResponseDtoList(unassigned);
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/assign")
    public ResponseEntity<?> assignCase(@RequestParam Long caseId, @RequestParam String username) {
        caseService.attachCase(caseId,username);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<CaseResponseDto> updateCase(
            @PathVariable Long id,
            @RequestBody CaseDto dto
    ) {
        return caseService.updateCase(id, dto)
                .map(updatedDto -> ResponseEntity.ok(updatedDto))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
