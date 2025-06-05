package com.lawyer.belawyer.service.serviceImpl;

import com.lawyer.belawyer.data.dto.CaseDto;
import com.lawyer.belawyer.data.dto.CaseResponseDto;
import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.data.mapper.CaseMapper;
import com.lawyer.belawyer.repository.CaseRepository;
import com.lawyer.belawyer.repository.UserRepository;
import com.lawyer.belawyer.service.CaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CaseServiceImpl implements CaseService {

    private final CaseMapper caseMapper;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    public CaseServiceImpl(CaseMapper caseMapper, CaseRepository caseRepository, UserRepository userRepository) {
        this.caseMapper = caseMapper;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    public List<CaseResponseDto> getAllCases() {
        List<Case> cases = caseRepository.findAll();
        return caseMapper.toResponseDtoList(cases);
    }

    public Optional<CaseResponseDto> getCaseByInstitution(String place) {
        Optional<Case> caseOpt = caseRepository.findByInstitution(place);
        return caseOpt.map(caseMapper::toResponseDto);
    }

    public Optional<CaseResponseDto> getCaseById(Long id) {
        Optional<Case> caseOpt = caseRepository.findById(id);
        return caseOpt.map(caseMapper::toResponseDto);
    }

    @Override
    public Optional<CaseResponseDto> updateCase(Long id, CaseDto dto) {
        Optional<Case> existingOpt = caseRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Case existing = existingOpt.get();
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setInstitution(dto.getInstitution());
        existing.setStatus(dto.getStatus());
        Case saved = caseRepository.save(existing);

        CaseResponseDto responseDto = caseMapper.toResponseDto(saved);
        return Optional.of(responseDto);
    }

    public Case saveCase(CaseDto dto) {
        Case legalCase = caseMapper.toEntity(dto);

        return caseRepository.save(legalCase);
    }

    @Override
    public void attachCase(Long caseId,String username) {
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        legalCase.setUser(user);
        caseRepository.save(legalCase);
    }

    public void deleteCase(Long id) {
        caseRepository.deleteById(id);
    }

    public List<Case> getAllUnassignedCases() {
        return caseRepository.findByUserIsNull();
    }
}
