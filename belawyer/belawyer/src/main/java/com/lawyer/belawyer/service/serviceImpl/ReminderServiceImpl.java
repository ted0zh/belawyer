package com.lawyer.belawyer.service.serviceImpl;

import com.lawyer.belawyer.data.dto.ReminderDto;
import com.lawyer.belawyer.data.dto.ReminderResponseDto;
import com.lawyer.belawyer.data.entity.Reminder;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.data.mapper.ReminderMapper;
import com.lawyer.belawyer.repository.ReminderRepository;
import com.lawyer.belawyer.repository.UserRepository;
import com.lawyer.belawyer.service.ReminderService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final ReminderMapper reminderMapper;

    public ReminderServiceImpl(ReminderRepository reminderRepository,
                               UserRepository userRepository,
                               ReminderMapper reminderMapper) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.reminderMapper = reminderMapper;
    }


    public ReminderResponseDto saveReminder(ReminderDto dto) {
        Optional<User> targetOpt = userRepository.findByUsername(dto.getTargetUsername());
        if (targetOpt.isEmpty()) {
            throw new RuntimeException("Target user not found");
        }
        User targetUser = targetOpt.get();

        Reminder reminder = reminderMapper.toEntity(dto);
        reminder.setUser(targetUser);

        Reminder saved = reminderRepository.save(reminder);

        return reminderMapper.toResponseDto(saved);
    }


    public List<ReminderResponseDto> getRemindersForCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Reminder> all = reminderRepository.findAll();

        return all.stream()
                .filter(r -> r.getUser().getUsername().equals(currentUsername))
                .map(reminderMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public ReminderResponseDto getReminderById(Long id) {
        Reminder r = reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        if (!r.getUser().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Access denied");
        }
        return reminderMapper.toResponseDto(r);
    }

    public void deleteReminder(Long id) {
        Optional<Reminder> rOpt = reminderRepository.findById(id);
        if (rOpt.isEmpty()) {
            return;
        }
        Reminder r = rOpt.get();

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        if (!r.getUser().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Access denied");
        }
        reminderRepository.delete(r);
    }

    @Override
    public List<Reminder> getRemindersByUsername(String username) {
        return List.of();
    }
}
