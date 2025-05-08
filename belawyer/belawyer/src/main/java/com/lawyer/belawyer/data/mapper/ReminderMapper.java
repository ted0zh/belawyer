package com.lawyer.belawyer.data.mapper;

import com.lawyer.belawyer.data.dto.ReminderDto;
import com.lawyer.belawyer.data.entity.Reminder;
import org.springframework.stereotype.Component;

@Component
public class ReminderMapper {

    public ReminderDto toDto(Reminder reminder){
        if(reminder==null){
            return null;
        }

        return new ReminderDto(
                reminder.getTitle(),
                reminder.getReminderDate()
        );
    }

    public Reminder toEntity(ReminderDto dto){
        if(dto==null){
            return null;
        }
        Reminder reminder = new Reminder();
        reminder.setReminderDate(dto.getReminderDate());
        reminder.setTitle(dto.getTitle());

        return reminder;
    }
}
