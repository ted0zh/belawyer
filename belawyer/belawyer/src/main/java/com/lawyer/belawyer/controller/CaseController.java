package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.CaseDto;
import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.service.serviceImpl.CaseServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/get")
    public ResponseEntity<Case> get(@RequestParam Long id){
        Optional<Case> caseOpt = caseService.getCaseById(id);
        if(caseOpt.isPresent()){
            return ResponseEntity.ok(caseOpt.get());
        }else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<Case>> getAll(){
        return ResponseEntity.ok(caseService.getAllCases());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam Long id){
        Optional<Case> caseOpt = caseService.getCaseById(id);
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

    @PutMapping("/assign")
    public ResponseEntity<?> assignCase(@RequestParam Long caseId, @RequestParam String username) {
        caseService.attachCase(caseId,username);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
