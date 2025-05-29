package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.CaseDto;
import com.lawyer.belawyer.data.dto.CaseResponseDto;
import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.entity.Role;
import com.lawyer.belawyer.service.serviceImpl.CaseServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/case")
public class CaseController {
    private final CaseServiceImpl caseService;
    public CaseController(CaseServiceImpl caseService){
        this.caseService=caseService;
    }

    @PostMapping("/create")
    public ResponseEntity<Case> create(@RequestBody CaseDto dto){
        return ResponseEntity.ok(caseService.saveCase(dto));
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<CaseResponseDto>> getAll(){
        return ResponseEntity.ok(caseService.getAllCases()); // Assuming a new method in service
    }

    @GetMapping("/get/byInstitution")
    public ResponseEntity<CaseResponseDto> get(@RequestParam String place){
        Optional<CaseResponseDto> caseOpt = caseService.getCaseByInstitution(place);
        if(caseOpt.isPresent()){
            return ResponseEntity.ok(caseOpt.get());
        }else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
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
    public ResponseEntity<List<Case>> getUnassignedCases() {
        return ResponseEntity.ok(caseService.getAllUnassignedCases());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/assign")
    public ResponseEntity<?> assignCase(@RequestParam Long caseId, @RequestParam String username) {
        caseService.attachCase(caseId,username);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
