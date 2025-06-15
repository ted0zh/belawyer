package com.lawyer.belawyer.serviceTests;

import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.data.mapper.UserMapper;
import com.lawyer.belawyer.repository.UserRepository;
import com.lawyer.belawyer.service.serviceImpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto sampleDto;
    private User sampleEntity;

    @BeforeEach
    void setUp() {
        sampleDto = new UserDto();
        sampleDto.setUsername("john_doe");
        sampleDto.setEmail("john@example.com");

        sampleEntity = new User();
        sampleEntity.setUsername("john_doe");
        sampleEntity.setEmail("john@example.com");
    }

    @Test
    void testCreateUser_mapsDtoToEntityAndSaves() {
        when(userMapper.toEntity(sampleDto)).thenReturn(sampleEntity);
        User savedEntity = new User();
        savedEntity.setId(5L);
        savedEntity.setUsername("john_doe");
        savedEntity.setEmail("john@example.com");
        when(userRepository.save(sampleEntity)).thenReturn(savedEntity);

        User result = userService.createUser(sampleDto);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("john_doe", result.getUsername());
        assertEquals("john@example.com", result.getEmail());

        verify(userMapper, times(1)).toEntity(sampleDto);
        verify(userRepository, times(1)).save(sampleEntity);
    }

    @Test
    void testUpdateUser_existingUser_updatesAndReturns() {
        String existingUsername = "john_doe";
        User existingUser = new User();
        existingUser.setId(10L);
        existingUser.setUsername(existingUsername);
        existingUser.setEmail("old@example.com");

        UserDto updateDto = new UserDto();
        updateDto.setUsername("john_doe_new");
        updateDto.setEmail("new@example.com");

        User updatedUserEntity = new User();
        updatedUserEntity.setId(10L);
        updatedUserEntity.setUsername("john_doe_new");
        updatedUserEntity.setEmail("new@example.com");

        when(userRepository.findByUsername(existingUsername)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUserEntity);

        User result = userService.updateUser(existingUsername, updateDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("john_doe_new", result.getUsername());
        assertEquals("new@example.com", result.getEmail());

        verify(userRepository, times(1)).findByUsername(existingUsername);
        assertEquals("john_doe_new", existingUser.getUsername());
        assertEquals("new@example.com", existingUser.getEmail());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testUpdateUser_userNotFound_returnsNull() {
        String nonExistent = "unknown";
        UserDto updateDto = new UserDto();
        updateDto.setUsername("whatever");
        updateDto.setEmail("whatever@example.com");

        when(userRepository.findByUsername(nonExistent)).thenReturn(Optional.empty());

        User result = userService.updateUser(nonExistent, updateDto);

        assertNull(result);
        verify(userRepository, times(1)).findByUsername(nonExistent);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testFetchUsersDto_returnsMappedList() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("alice");
        user1.setEmail("alice@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("bob");
        user2.setEmail("bob@example.com");

        UserDto dto1 = new UserDto();
        dto1.setUsername("alice");
        dto1.setEmail("alice@example.com");

        UserDto dto2 = new UserDto();
        dto2.setUsername("bob");
        dto2.setEmail("bob@example.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        List<UserDto> result = userService.fetchUsersDto();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(dto1, dto2)));

        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).toDto(user1);
        verify(userMapper, times(1)).toDto(user2);
    }

    @Test
    void testGetUser_returnsOptionalFromRepository() {
        Long userId = 42L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUser(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUser_notFound_returnsEmpty() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUser(userId);

        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testDeleteUser_existingUser_deletes() {
        String usernameToDelete = "to_delete";
        User existing = new User();
        existing.setId(7L);
        existing.setUsername(usernameToDelete);

        when(userRepository.findByUsername(usernameToDelete)).thenReturn(Optional.of(existing));

        assertDoesNotThrow(() -> userService.deleteUser(usernameToDelete));

        verify(userRepository, times(1)).findByUsername(usernameToDelete);
        verify(userRepository, times(1)).delete(existing);
    }

    @Test
    void testDeleteUser_userNotFound_throwsException() {
        String nonexistent = "ghost";
        when(userRepository.findByUsername(nonexistent)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.deleteUser(nonexistent));
        assertTrue(ex.getMessage().contains("User not found: " + nonexistent));

        verify(userRepository, times(1)).findByUsername(nonexistent);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void testFindByUsername_existingUser_returnsUser() {
        String username = "jane";
        User user = new User();
        user.setId(15L);
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = userService.findByUsername(username);

        assertNotNull(result);
        assertEquals(15L, result.getId());
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testFindByUsername_userNotFound_throwsNoSuchElementException() {
        String missing = "nobody";
        when(userRepository.findByUsername(missing)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> userService.findByUsername(missing));
        verify(userRepository, times(1)).findByUsername(missing);
    }
}
