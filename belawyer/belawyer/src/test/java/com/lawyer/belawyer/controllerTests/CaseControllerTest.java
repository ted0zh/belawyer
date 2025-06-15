package com.lawyer.belawyer.controllerTests;

import com.lawyer.belawyer.controller.CaseController;
import com.lawyer.belawyer.data.dto.CaseDto;
import com.lawyer.belawyer.data.dto.CaseResponseDto;
import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.mapper.CaseMapper;
import com.lawyer.belawyer.service.serviceImpl.CaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CaseControllerTest {

    @Mock
    private CaseServiceImpl caseService;
    @Mock
    private CaseMapper caseMapper;

    @InjectMocks
    private CaseController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_returns200AndBody() {
        CaseDto dto = new CaseDto();
        dto.setTitle("Test Title");
        dto.setDescription("Test Description");
        dto.setInstitution("Test Institution");
        dto.setStatus("OPEN");

        Case saved = new Case();
        saved.setId(100L);
        saved.setTitle("Test Title");
        saved.setDescription("Test Description");
        saved.setInstitution("Test Institution");
        saved.setStatus("OPEN");

        when(caseService.saveCase(dto)).thenReturn(saved);

        ResponseEntity<Case> response = controller.create(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(saved, response.getBody());

        verify(caseService, times(1)).saveCase(dto);
    }

    @Test
    void fetchAll_returnsListOfDtos() {
        CaseResponseDto dto1 = new CaseResponseDto();
        dto1.setId(1L);
        dto1.setTitle("A");
        dto1.setDescription("Desc A");
        dto1.setInstitution("Inst A");
        dto1.setStatus("OPEN");

        CaseResponseDto dto2 = new CaseResponseDto();
        dto2.setId(2L);
        dto2.setTitle("B");
        dto2.setDescription("Desc B");
        dto2.setInstitution("Inst B");
        dto2.setStatus("CLOSED");

        List<CaseResponseDto> list = List.of(dto1, dto2);
        when(caseService.getAllCases()).thenReturn(list);

        ResponseEntity<List<CaseResponseDto>> response = controller.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());

        verify(caseService, times(1)).getAllCases();
    }

    @Test
    void getByInstitution_found_returns200AndDto() {
        String place = "InstX";

        CaseResponseDto dto = new CaseResponseDto();
        dto.setId(10L);
        dto.setTitle("X");
        dto.setDescription("Desc X");
        dto.setInstitution(place);
        dto.setStatus("PENDING");

        List<CaseResponseDto> dtoList = List.of(dto);

        when(caseService.getCaseByInstitution(place)).thenReturn(dtoList);

        ResponseEntity<List<CaseResponseDto>> response = controller.get(place);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtoList, response.getBody());

        verify(caseService, times(1)).getCaseByInstitution(place);
    }

    @Test
    void getByInstitution_notFound_returns204() {
        String place = "Unknown";

        when(caseService.getCaseByInstitution(place)).thenReturn(Collections.emptyList());

        ResponseEntity<List<CaseResponseDto>> response = controller.get(place);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(caseService, times(1)).getCaseByInstitution(place);
    }


    @Test
    void getById_found_returns200AndDto() {
        Long id = 20L;
        CaseResponseDto dto = new CaseResponseDto();
        dto.setId(id);
        dto.setTitle("Y");
        dto.setDescription("Desc Y");
        dto.setInstitution("InstY");
        dto.setStatus("RESOLVED");

        when(caseService.getCaseById(id)).thenReturn(Optional.of(dto));

        ResponseEntity<CaseResponseDto> response = controller.get(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(caseService, times(1)).getCaseById(id);
    }

    @Test
    void getById_notFound_returns204() {
        Long id = 999L;
        when(caseService.getCaseById(id)).thenReturn(Optional.empty());

        ResponseEntity<CaseResponseDto> response = controller.get(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(caseService, times(1)).getCaseById(id);
    }

    @Test
    void delete_found_returns200() {
        Long id = 30L;
        CaseResponseDto dto = new CaseResponseDto();
        dto.setId(id);
        when(caseService.getCaseById(id)).thenReturn(Optional.of(dto));

        ResponseEntity<?> response = controller.delete(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(caseService, times(1)).getCaseById(id);
        verify(caseService, times(1)).deleteCase(id);
    }

    @Test
    void delete_notFound_returns204() {
        Long id = 40L;
        when(caseService.getCaseById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(caseService, times(1)).getCaseById(id);
        verify(caseService, never()).deleteCase(anyLong());
    }

    @Test
    void getUnassignedCases_returnsListAnd200() {
        Case c1 = new Case();
        c1.setId(50L);
        c1.setTitle("Unassigned1");

        Case c2 = new Case();
        c2.setId(51L);
        c2.setTitle("Unassigned2");

        List<Case> entityList = List.of(c1, c2);

        CaseResponseDto dto1 = new CaseResponseDto();
        dto1.setId(50L);
        dto1.setTitle("Unassigned1");

        CaseResponseDto dto2 = new CaseResponseDto();
        dto2.setId(51L);
        dto2.setTitle("Unassigned2");

        List<CaseResponseDto> dtoList = List.of(dto1, dto2);

        when(caseService.getAllUnassignedCases()).thenReturn(entityList);
        when(caseMapper.toResponseDtoList(entityList)).thenReturn(dtoList);

        ResponseEntity<List<CaseResponseDto>> response = controller.getUnassignedCases();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dtoList, response.getBody());

        verify(caseService, times(1)).getAllUnassignedCases();
        verify(caseMapper, times(1)).toResponseDtoList(entityList);
    }

    @Test
    void assignCase_always_returns200() {
        Long caseId = 60L;
        String username = "jane";

        ResponseEntity<?> response = controller.assignCase(caseId, username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(caseService, times(1)).attachCase(caseId, username);
    }

    @Test
    void updateCase_found_returns200AndDto() {
        Long id = 70L;
        CaseDto dto = new CaseDto();
        dto.setTitle("Updated Title");
        dto.setDescription("Updated Desc");
        dto.setInstitution("Updated Inst");
        dto.setStatus("UPDATED");

        CaseResponseDto responseDto = new CaseResponseDto();
        responseDto.setId(id);
        responseDto.setTitle("Updated Title");
        responseDto.setDescription("Updated Desc");
        responseDto.setInstitution("Updated Inst");
        responseDto.setStatus("UPDATED");

        when(caseService.updateCase(eq(id), any(CaseDto.class)))
                .thenReturn(Optional.of(responseDto));

        ResponseEntity<CaseResponseDto> response = controller.updateCase(id, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDto, response.getBody());

        verify(caseService, times(1)).updateCase(eq(id), any(CaseDto.class));
    }

    @Test
    void updateCase_notFound_returns404() {
        Long id = 80L;
        CaseDto dto = new CaseDto();
        dto.setTitle("Nope");
        dto.setDescription("Nope");
        dto.setInstitution("Nope");
        dto.setStatus("Nope");

        when(caseService.updateCase(eq(id), any(CaseDto.class)))
                .thenReturn(Optional.empty());

        ResponseEntity<CaseResponseDto> response = controller.updateCase(id, dto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(caseService, times(1)).updateCase(eq(id), any(CaseDto.class));
    }
}
