package com.lawyer.belawyer.controllerTests;

import com.lawyer.belawyer.controller.ReminderController;
import com.lawyer.belawyer.data.dto.ReminderDto;
import com.lawyer.belawyer.data.dto.ReminderResponseDto;
import com.lawyer.belawyer.service.serviceImpl.ReminderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReminderControllerTest {

    @Mock
    private ReminderServiceImpl reminderService;

    @InjectMocks
    private ReminderController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldReturn200AndCreatedDto() {
        ReminderDto dto = new ReminderDto();
        dto.setTargetUsername("alice");
        dto.setTitle("Finish report");
        dto.setReminderDate(LocalDate.of(2025, 6, 10));
        dto.setReminderTime(LocalTime.of(14, 0));

        ReminderResponseDto responseDto = new ReminderResponseDto();
        responseDto.setId(1L);
        responseDto.setTargetUsername("alice");
        responseDto.setTitle("Finish report");
        responseDto.setReminderDate(LocalDate.of(2025, 6, 10));
        responseDto.setReminderTime(LocalTime.of(14, 0));

        when(reminderService.saveReminder(dto)).thenReturn(responseDto);

        ResponseEntity<ReminderResponseDto> response = controller.create(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDto, response.getBody());

        verify(reminderService, times(1)).saveReminder(dto);
    }

    @Test
    void getAll_shouldReturn200AndList() {
        ReminderResponseDto dto1 = new ReminderResponseDto();
        dto1.setId(1L);
        dto1.setTargetUsername("alice");
        dto1.setTitle("Task 1");
        dto1.setReminderDate(LocalDate.of(2025, 6, 11));
        dto1.setReminderTime(LocalTime.of(9, 0));

        ReminderResponseDto dto2 = new ReminderResponseDto();
        dto2.setId(2L);
        dto2.setTargetUsername("alice");
        dto2.setTitle("Task 2");
        dto2.setReminderDate(LocalDate.of(2025, 6, 12));
        dto2.setReminderTime(LocalTime.of(10, 0));

        List<ReminderResponseDto> list = List.of(dto1, dto2);
        when(reminderService.getRemindersForCurrentUser()).thenReturn(list);

        ResponseEntity<List<ReminderResponseDto>> response = controller.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());

        verify(reminderService, times(1)).getRemindersForCurrentUser();
    }

    @Test
    void getOne_shouldReturn200AndDto() {
        Long id = 5L;
        ReminderResponseDto dto = new ReminderResponseDto();
        dto.setId(id);
        dto.setTargetUsername("bob");
        dto.setTitle("Call client");
        dto.setReminderDate(LocalDate.of(2025, 6, 13));
        dto.setReminderTime(LocalTime.of(11, 0));

        when(reminderService.getReminderById(id)).thenReturn(dto);

        ResponseEntity<ReminderResponseDto> response = controller.getOne(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(reminderService, times(1)).getReminderById(id);
    }

    @Test
    void delete_shouldReturn200AndInvokeService() {
        Long id = 7L;

        ResponseEntity<?> response = controller.delete(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(reminderService, times(1)).deleteReminder(id);
    }
}

