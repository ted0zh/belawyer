package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.ReminderDto;
import com.lawyer.belawyer.data.dto.ReminderResponseDto;
import com.lawyer.belawyer.service.serviceImpl.ReminderServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reminder")
public class ReminderController {

    private final ReminderServiceImpl reminderService;

    public ReminderController(ReminderServiceImpl reminderService) {
        this.reminderService = reminderService;
    }

    //create new reminder
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ResponseEntity<ReminderResponseDto> create(@RequestBody ReminderDto dto) {
        ReminderResponseDto created = reminderService.saveReminder(dto);
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all")
    public ResponseEntity<List<ReminderResponseDto>> getAll() {
        List<ReminderResponseDto> list = reminderService.getRemindersForCurrentUser();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponseDto> getOne(@PathVariable("id") Long id) {
        ReminderResponseDto dto = reminderService.getReminderById(id);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.ok().build();
    }
}
