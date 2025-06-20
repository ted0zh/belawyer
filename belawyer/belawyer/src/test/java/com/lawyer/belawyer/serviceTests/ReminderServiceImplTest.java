package com.lawyer.belawyer.serviceTests;

import com.lawyer.belawyer.data.dto.ReminderDto;
import com.lawyer.belawyer.data.dto.ReminderResponseDto;
import com.lawyer.belawyer.data.entity.Reminder;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.data.mapper.ReminderMapper;
import com.lawyer.belawyer.repository.ReminderRepository;
import com.lawyer.belawyer.repository.UserRepository;
import com.lawyer.belawyer.service.serviceImpl.ReminderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ReminderServiceImplTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReminderMapper reminderMapper;

    @InjectMocks
    private ReminderServiceImpl reminderService;

    private User targetUser;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();

        targetUser = new User();
        targetUser.setId(100L);
        targetUser.setUsername("targetUser");

        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User(
                        "currentUser",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(springUser, null, springUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testSaveReminder_successful() {
        ReminderDto dto = new ReminderDto();
        dto.setTargetUsername("targetUser");
        dto.setTitle("Remember this");
        dto.setReminderDate(LocalDate.of(2025, 6, 10));
        dto.setReminderTime(LocalTime.of(9, 30));

        Reminder toSave = new Reminder();
        Reminder saved = new Reminder();
        saved.setId(1L);
        saved.setTitle(dto.getTitle());
        saved.setReminderDate(dto.getReminderDate());
        saved.setReminderTime(dto.getReminderTime());

        saved.setUser(targetUser);

        ReminderResponseDto responseDto = new ReminderResponseDto();
        responseDto.setId(1L);
        responseDto.setTitle(dto.getTitle());
        responseDto.setReminderDate(dto.getReminderDate());
        responseDto.setReminderTime(dto.getReminderTime());
        responseDto.setTargetUsername("targetUser");

        when(userRepository.findByUsername("targetUser"))
                .thenReturn(Optional.of(targetUser));
        when(reminderMapper.toEntity(dto)).thenReturn(toSave);
        when(reminderRepository.save(toSave)).thenReturn(saved);
        when(reminderMapper.toResponseDto(saved)).thenReturn(responseDto);

        ReminderResponseDto result = reminderService.saveReminder(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Remember this", result.getTitle());
        assertEquals(LocalDate.of(2025, 6, 10), result.getReminderDate());
        assertEquals(LocalTime.of(9, 30), result.getReminderTime());
        assertEquals("targetUser", result.getTargetUsername());

        verify(userRepository, times(1)).findByUsername("targetUser");
        verify(reminderMapper, times(1)).toEntity(dto);
        verify(reminderRepository, times(1)).save(toSave);
        verify(reminderMapper, times(1)).toResponseDto(saved);
    }


    @Test
    void testSaveReminder_targetUserNotFound_throwsException() {
        ReminderDto dto = new ReminderDto();
        dto.setTargetUsername("nonexistentUser");
        when(userRepository.findByUsername("nonexistentUser")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reminderService.saveReminder(dto));
        assertTrue(ex.getMessage().contains("Target user not found"));

        verify(userRepository, times(1)).findByUsername("nonexistentUser");
        verifyNoMoreInteractions(reminderMapper, reminderRepository);
    }

    @Test
    void testGetRemindersForCurrentUser_returnsOnlyCurrentUsers() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(200L);
        otherUser.setUsername("otherUser");

        Reminder r1 = new Reminder();
        r1.setId(10L);
        r1.setUser(targetUser);

        Reminder r2 = new Reminder();
        r2.setId(11L);
        r2.setUser(new User() {{ setUsername("currentUser"); }});

        List<Reminder> allReminders = List.of(r1, r2);

        ReminderResponseDto dto1 = new ReminderResponseDto();
        dto1.setId(11L);
        dto1.setTargetUsername("currentUser");

        when(userRepository.findByUsername("currentUser"))
                .thenReturn(Optional.of(new User() {{ setUsername("currentUser"); }}));
        when(reminderRepository.findAll()).thenReturn(allReminders);
        when(reminderMapper.toResponseDto(r2)).thenReturn(dto1);

        List<ReminderResponseDto> result = reminderService.getRemindersForCurrentUser();

        assertEquals(1, result.size());
        assertEquals(11L, result.get(0).getId());
        assertEquals("currentUser", result.get(0).getTargetUsername());

        verify(reminderRepository, times(1)).findAll();
        verify(reminderMapper, times(1)).toResponseDto(r2);
    }

    @Test
    void testGetReminderById_successful() {
        Long remId = 20L;
        Reminder r = new Reminder();
        r.setId(remId);
        r.setUser(new User() {{ setUsername("currentUser"); }});

        ReminderResponseDto responseDto = new ReminderResponseDto();
        responseDto.setId(remId);
        responseDto.setTargetUsername("currentUser");

        when(reminderRepository.findById(remId)).thenReturn(Optional.of(r));
        when(reminderMapper.toResponseDto(r)).thenReturn(responseDto);

        ReminderResponseDto result = reminderService.getReminderById(remId);

        assertNotNull(result);
        assertEquals(remId, result.getId());
        assertEquals("currentUser", result.getTargetUsername());

        verify(reminderRepository, times(1)).findById(remId);
        verify(reminderMapper, times(1)).toResponseDto(r);
    }

    @Test
    void testGetReminderById_notFound_throwsException() {
        Long remId = 21L;
        when(reminderRepository.findById(remId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reminderService.getReminderById(remId));
        assertTrue(ex.getMessage().contains("Reminder not found"));

        verify(reminderRepository, times(1)).findById(remId);
        verifyNoMoreInteractions(reminderMapper);
    }

    @Test
    void testGetReminderById_accessDenied_throwsException() {
        Long remId = 22L;
        Reminder r = new Reminder();
        r.setId(remId);
        r.setUser(new User() {{ setUsername("otherUser"); }});

        when(reminderRepository.findById(remId)).thenReturn(Optional.of(r));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reminderService.getReminderById(remId));
        assertTrue(ex.getMessage().contains("Access denied"));

        verify(reminderRepository, times(1)).findById(remId);
        verifyNoMoreInteractions(reminderMapper);
    }

    @Test
    void testDeleteReminder_notFound_noException() {
        Long remId = 30L;
        when(reminderRepository.findById(remId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> reminderService.deleteReminder(remId));
        verify(reminderRepository, times(1)).findById(remId);
        verify(reminderRepository, never()).delete(any(Reminder.class));
    }

    @Test
    void testDeleteReminder_accessDenied_throwsException() {
        Long remId = 31L;
        Reminder r = new Reminder();
        r.setId(remId);
        r.setUser(new User() {{ setUsername("otherUser"); }});

        when(reminderRepository.findById(remId)).thenReturn(Optional.of(r));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reminderService.deleteReminder(remId));
        assertTrue(ex.getMessage().contains("Access denied"));

        verify(reminderRepository, times(1)).findById(remId);
        verify(reminderRepository, never()).delete(r);
    }

    @Test
    void testDeleteReminder_successfulDelete() {
        Long remId = 32L;
        Reminder r = new Reminder();
        r.setId(remId);
        r.setUser(new User() {{ setUsername("currentUser"); }});

        when(reminderRepository.findById(remId)).thenReturn(Optional.of(r));

        reminderService.deleteReminder(remId);

        verify(reminderRepository, times(1)).findById(remId);
        verify(reminderRepository, times(1)).delete(r);
    }

    @Test
    void testGetRemindersByUsername_returnsEmptyList() {
        List<Reminder> result = reminderService.getRemindersByUsername("anyUser");
        assertTrue(result.isEmpty());
    }
}
