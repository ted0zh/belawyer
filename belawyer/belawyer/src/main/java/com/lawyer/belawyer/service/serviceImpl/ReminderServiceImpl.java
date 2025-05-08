package com.lawyer.belawyer.service.serviceImpl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lawyer.belawyer.data.dto.ReminderDto;
import com.lawyer.belawyer.data.entity.Reminder;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.data.mapper.ReminderMapper;
import com.lawyer.belawyer.repository.ReminderRepository;
import com.lawyer.belawyer.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReminderServiceImpl {
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final ReminderMapper reminderMapper;


    public ReminderServiceImpl(ReminderRepository reminderRepository, UserRepository userRepository, ReminderMapper reminderMapper) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.reminderMapper = reminderMapper;
    }

    public List<ReminderDto> getRemindersByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));

        return user.getReminders().stream()
                .map(reminder -> new ReminderDto(reminder.getTitle(), reminder.getReminderDate()))
                .collect(Collectors.toList());
//        Optional<User> userOpt = userRepository.findByUsername(username);
//        return userOpt.get().getReminders().stream()
//                .filter(r->r.getUser().equals(username))
//                .limit(10)
//                .toList();
    }

    public Reminder saveReminder(ReminderDto dto, String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        User user = userOpt.get();
        Reminder reminder = reminderMapper.toEntity(dto);
        reminder.setUser(user);

        return reminderRepository.save(reminder);
    }
}
