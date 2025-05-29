package com.lawyer.belawyer.service.serviceImpl;

import com.lawyer.belawyer.data.dto.CaseDto;
import com.lawyer.belawyer.data.dto.CaseResponseDto;
import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.data.mapper.CaseMapper;
import com.lawyer.belawyer.repository.CaseRepository;
import com.lawyer.belawyer.repository.UserRepository;
import com.lawyer.belawyer.service.CaseService;
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
    public Case saveCase(CaseDto dto) {
        Case legalCase = caseMapper.toEntity(dto);

        return caseRepository.save(legalCase);
    }

    @Override
    public void attachCase(Long caseId,String username) {
        Optional<Case> legalCaseOpt  = caseRepository.findById(caseId);
        Optional<User> user =  userRepository.findByUsername(username);
        Case legalCase = legalCaseOpt.get();
        legalCase.setUser(user.get());

        caseRepository.save(legalCase);
    }

    public void deleteCase(Long id) {
        caseRepository.deleteById(id);
    }

    public List<Case> getAllUnassignedCases() {
        return caseRepository.findByUserIsNull();
    }
}
