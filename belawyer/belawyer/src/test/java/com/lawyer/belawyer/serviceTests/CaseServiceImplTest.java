package com.lawyer.belawyer.serviceTests;

import com.lawyer.belawyer.data.dto.CaseDto;
import com.lawyer.belawyer.data.dto.CaseResponseDto;
import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.data.mapper.CaseMapper;
import com.lawyer.belawyer.repository.CaseRepository;
import com.lawyer.belawyer.repository.UserRepository;
import com.lawyer.belawyer.service.serviceImpl.CaseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceImplTest {

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CaseServiceImpl caseService;

    @Test
    void testGetAllCases_returnsMappedList() {
        Case case1 = new Case();
        Case case2 = new Case();
        List<Case> caseList = Arrays.asList(case1, case2);

        CaseResponseDto dto1 = new CaseResponseDto();
        CaseResponseDto dto2 = new CaseResponseDto();
        List<CaseResponseDto> dtoList = Arrays.asList(dto1, dto2);

        when(caseRepository.findAll()).thenReturn(caseList);
        when(caseMapper.toResponseDtoList(caseList)).thenReturn(dtoList);

        List<CaseResponseDto> result = caseService.getAllCases();

        assertEquals(dtoList, result);
        verify(caseRepository, times(1)).findAll();
        verify(caseMapper, times(1)).toResponseDtoList(caseList);
    }

//    @Test
//    void testGetCaseByInstitution_whenFound_returnsDto() {
//        // Arrange
//        String institution = "Supreme Court";
//        Case foundCase = new Case();
//        CaseResponseDto mappedDto = new CaseResponseDto();
//
//        when(caseRepository.findByInstitution(institution)).thenReturn(Optional.of(foundCase));
//        when(caseMapper.toResponseDto(foundCase)).thenReturn(mappedDto);
//
//        // Act
//        Optional<CaseResponseDto> result = caseService.getCaseByInstitution(institution);
//
//        // Assert
//        assertTrue(result.isPresent());
//        assertEquals(mappedDto, result.get());
//        verify(caseRepository, times(1)).findByInstitution(institution);
//        verify(caseMapper, times(1)).toResponseDto(foundCase);
//    }

//    @Test
//    void testGetCaseByInstitution_whenNotFound_returnsEmpty() {
//        // Arrange
//        String institution = "Nonexistent Institution";
//        when(caseRepository.findByInstitution(institution)).thenReturn(Optional.empty());
//
//        // Act
//        Optional<CaseResponseDto> result = caseService.getCaseByInstitution(institution);
//
//        // Assert
//        assertTrue(result.isEmpty());
//        verify(caseRepository, times(1)).findByInstitution(institution);
//        verify(caseMapper, never()).toResponseDto(any());
//    }

    @Test
    void testGetCaseById_whenFound_returnsDto() {

        Long id = 42L;
        Case foundCase = new Case();
        CaseResponseDto mappedDto = new CaseResponseDto();

        when(caseRepository.findById(id)).thenReturn(Optional.of(foundCase));
        when(caseMapper.toResponseDto(foundCase)).thenReturn(mappedDto);

        Optional<CaseResponseDto> result = caseService.getCaseById(id);

        assertTrue(result.isPresent());
        assertEquals(mappedDto, result.get());
        verify(caseRepository, times(1)).findById(id);
        verify(caseMapper, times(1)).toResponseDto(foundCase);
    }

    @Test
    void testGetCaseById_whenNotFound_returnsEmpty() {
        Long id = 99L;
        when(caseRepository.findById(id)).thenReturn(Optional.empty());

        Optional<CaseResponseDto> result = caseService.getCaseById(id);

        assertTrue(result.isEmpty());
        verify(caseRepository, times(1)).findById(id);
        verify(caseMapper, never()).toResponseDto(any());
    }

    @Test
    void testUpdateCase_whenNotFound_returnsEmpty() {
        Long id = 123L;
        CaseDto dto = new CaseDto();
        when(caseRepository.findById(id)).thenReturn(Optional.empty());

        Optional<CaseResponseDto> result = caseService.updateCase(id, dto);

        assertTrue(result.isEmpty());
        verify(caseRepository, times(1)).findById(id);
        verify(caseRepository, never()).save(any());
        verify(caseMapper, never()).toResponseDto(any());
    }

    @Test
    void testUpdateCase_whenFound_updatesFieldsAndReturnsDto() {
        Long id = 5L;
        Case existing = new Case();
        existing.setTitle("Old Title");
        existing.setDescription("Old Description");
        existing.setInstitution("Old Institution");
        existing.setStatus("Open");

        CaseDto dto = new CaseDto();
        dto.setTitle("New Title");
        dto.setDescription("New Description");
        dto.setInstitution("New Institution");
        dto.setStatus("Closed");

        CaseResponseDto responseDto = new CaseResponseDto();

        when(caseRepository.findById(id)).thenReturn(Optional.of(existing));
        when(caseRepository.save(existing)).thenReturn(existing);
        when(caseMapper.toResponseDto(existing)).thenReturn(responseDto);

        Optional<CaseResponseDto> result = caseService.updateCase(id, dto);

        assertTrue(result.isPresent());
        assertEquals(responseDto, result.get());

        assertEquals("New Title", existing.getTitle());
        assertEquals("New Description", existing.getDescription());
        assertEquals("New Institution", existing.getInstitution());
        assertEquals("Closed", existing.getStatus());

        verify(caseRepository, times(1)).findById(id);
        verify(caseRepository, times(1)).save(existing);
        verify(caseMapper, times(1)).toResponseDto(existing);
    }

    @Test
    void testSaveCase_mapsDtoToEntityAndSaves() {
        CaseDto dto = new CaseDto();
        dto.setTitle("Test Case");
        dto.setDescription("Test Description");
        dto.setInstitution("Test Institution");
        dto.setStatus("Pending");

        Case mappedEntity = new Case();
        Case savedEntity = new Case();
        savedEntity.setId(77L);

        when(caseMapper.toEntity(dto)).thenReturn(mappedEntity);
        when(caseRepository.save(mappedEntity)).thenReturn(savedEntity);

        Case result = caseService.saveCase(dto);

        assertEquals(savedEntity, result);
        verify(caseMapper, times(1)).toEntity(dto);
        verify(caseRepository, times(1)).save(mappedEntity);
    }

    @Test
    void testAttachCase_assignsUserAndSaves() {
        Long caseId = 10L;
        String username = "john_doe";

        Case existingCase = new Case();
        existingCase.setId(caseId);

        User user = new User();
        user.setId(99L);
        user.setUsername(username);

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(existingCase));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(caseRepository.save(existingCase)).thenReturn(existingCase);

        caseService.attachCase(caseId, username);

        assertEquals(user, existingCase.getUser());
        verify(caseRepository, times(1)).findById(caseId);
        verify(userRepository, times(1)).findByUsername(username);
        verify(caseRepository, times(1)).save(existingCase);
    }

    @Test
    void testAttachCase_whenCaseNotFound_throwsException() {
        Long caseId = 1234L;
        String username = "alice";

        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> caseService.attachCase(caseId, username));
        verify(caseRepository, times(1)).findById(caseId);
        verify(userRepository, never()).findByUsername(any());
        verify(caseRepository, never()).save(any());
    }

    @Test
    void testDeleteCase_invokesRepositoryDelete() {
        Long idToDelete = 20L;

        caseService.deleteCase(idToDelete);

        verify(caseRepository, times(1)).deleteById(idToDelete);
    }

    @Test
    void testGetAllUnassignedCases_returnsList() {
        Case unassigned1 = new Case();
        Case unassigned2 = new Case();
        List<Case> unassignedCases = Arrays.asList(unassigned1, unassigned2);

        when(caseRepository.findByUserIsNull()).thenReturn(unassignedCases);

        List<Case> result = caseService.getAllUnassignedCases();

        assertEquals(unassignedCases, result);
        verify(caseRepository, times(1)).findByUserIsNull();
    }
}

