package com.lawyer.belawyer.data.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class ReminderResponseDto {
    private Long id;
    private String title;
    private LocalDate reminderDate;
    private LocalTime reminderTime;
    private boolean sent;
    private Long caseId;
    private String targetUsername;
}
