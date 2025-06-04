//package com.lawyer.belawyer.data.mapper;
//
//import com.lawyer.belawyer.data.dto.ReminderDto;
//import com.lawyer.belawyer.data.entity.Reminder;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import com.lawyer.belawyer.data.entity.Case;
//@Component
//public class ReminderMapper {
//
//    /**
//     * Преобразува Entity → DTO (за отговор).
//     */
//    public ReminderDto toDto(Reminder reminder) {
//        if (reminder == null) {
//            return null;
//        }
//
//        // id винаги връщаме (ако е записано в базата)
//        ReminderDto dto = new ReminderDto();
//        dto.setId(reminder.getId());
//        dto.setTitle(reminder.getTitle());
//        dto.setReminderDate(reminder.getReminderDate());
//        dto.setReminderTime(reminder.getReminderTime());
//
//        // Ако caseEntity != null, взимаме неговото id
//        if (reminder.getCaseEntity() != null) {
//            dto.setCaseId(reminder.getCaseEntity().getId());
//        }
//
//        return dto;
//    }
//
//    /**
//     * Преобразува входящ DTO → Entity (за създаване/обновяване).
//     * Забележка:
//     *   - Потребителят (user) се сетва в Service-а.
//     *   - CaseEntity се мапва чрез неговото id (в Service-а).
//     */
//    public Reminder toEntity(ReminderDto dto) {
//        if (dto == null) {
//            return null;
//        }
//
//        Reminder reminder = new Reminder();
//        // Ако искаме UPDATE, можем да сетнем id (но обикновено това става в Service-a при търсене в БД)
//        if (dto.getId() != null) {
//            reminder.setId(dto.getId());
//        }
//
//        reminder.setTitle(dto.getTitle());
//        reminder.setReminderDate(dto.getReminderDate());
//        reminder.setReminderTime(dto.getReminderTime());
//
//        // Единствено caseEntity – тук правим “placeholder” със само id,
//        // но в Service-а ще го заместваме с реалния обект (caseRepository.findById).
//        if (dto.getCaseId() != null) {
//            Case c = new Case();
//            c.setId(dto.getCaseId());
//            reminder.setCaseEntity(c);
//        }
//
//        // User-а ще се сетне директно в Service-а (от context-а)
//        return reminder;
//    }
//
//
//    public List<ReminderDto> toDtoList(List<Reminder> reminders) {
//        if (reminders == null) {
//            return null;
//        }
//        return reminders.stream()
//                .map(this::toDto)
//                .collect(Collectors.toList());
//    }
//}
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
        // sent по подразбиране е false
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

