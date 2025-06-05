package com.lawyer.belawyer.service;

import com.lawyer.belawyer.data.dto.ReminderDto;
import com.lawyer.belawyer.data.dto.ReminderResponseDto;
import com.lawyer.belawyer.data.entity.Reminder;

import java.util.List;

public interface ReminderService {

    List<Reminder> getRemindersByUsername(String username);
    ReminderResponseDto saveReminder(ReminderDto dto);
    void deleteReminder(Long id);
    ReminderResponseDto getReminderById(Long id);
}
