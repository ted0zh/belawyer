package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.ReminderDto;
import com.lawyer.belawyer.data.entity.Reminder;
import com.lawyer.belawyer.service.serviceImpl.ReminderServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/reminder")
public class ReminderController {
    private final ReminderServiceImpl reminderService;

    public ReminderController(ReminderServiceImpl reminderService) {
        this.reminderService = reminderService;
    }
    @RequestMapping("/setReminder")
    @PostMapping
    public ResponseEntity<Reminder> set(@RequestBody ReminderDto dto, @RequestParam String username){
        return ResponseEntity.ok(reminderService.saveReminder(dto,username));
    }
    @RequestMapping("/getReminder")
    public ResponseEntity<List<ReminderDto>> get(@RequestParam String username){
        return ResponseEntity.ok(reminderService.getRemindersByUsername(username));
    }
}
