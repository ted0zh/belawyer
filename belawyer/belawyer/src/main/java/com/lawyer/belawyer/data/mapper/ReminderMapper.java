package com.lawyer.belawyer.data.mapper;

import com.lawyer.belawyer.data.dto.ReminderDto;
import com.lawyer.belawyer.data.dto.ReminderResponseDto;
import com.lawyer.belawyer.data.entity.Reminder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReminderMapper {

    public ReminderDto toDto(Reminder reminder) {
        if (reminder == null) return null;
        return new ReminderDto(
                reminder.getTitle(),
                reminder.getReminderDate(),
                reminder.getReminderTime(),
                // front-end няма нужда от това при рендериране; полето е празно
                ""
        );
    }

    public Reminder toEntity(ReminderDto dto) {
        if (dto == null) return null;
        Reminder reminder = new Reminder();
        reminder.setTitle(dto.getTitle());
        reminder.setReminderDate(dto.getReminderDate());
        reminder.setReminderTime(dto.getReminderTime());
        return reminder;
    }

    public ReminderResponseDto toResponseDto(Reminder reminder) {
        if (reminder == null) return null;
        ReminderResponseDto dto = new ReminderResponseDto();
        dto.setId(reminder.getId());
        dto.setTitle(reminder.getTitle());
        dto.setReminderDate(reminder.getReminderDate());
        dto.setReminderTime(reminder.getReminderTime());
        dto.setSent(reminder.isSent());
        dto.setCaseId(reminder.getCaseEntity() != null ? reminder.getCaseEntity().getId() : null);
        dto.setTargetUsername(reminder.getUser().getUsername());
        return dto;
    }

    public List<ReminderResponseDto> toResponseDtoList(List<Reminder> reminders) {
        if (reminders == null) return null;
        return reminders.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}

