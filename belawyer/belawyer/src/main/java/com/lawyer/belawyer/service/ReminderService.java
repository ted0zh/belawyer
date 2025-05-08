package com.lawyer.belawyer.service;

import com.lawyer.belawyer.data.entity.Reminder;

import java.util.List;

public interface ReminderService {
    public List<Reminder> getRemindersByUsername(String username);

    public Reminder saveReminder(Reminder reminder,String username);
}
