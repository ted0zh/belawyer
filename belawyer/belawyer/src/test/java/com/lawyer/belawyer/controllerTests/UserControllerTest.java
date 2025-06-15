package com.lawyer.belawyer.controllerTests;

import com.lawyer.belawyer.controller.UserController;
import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.service.serviceImpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private UserController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_withValidDto_returns200AndUser() {
        UserDto dto = new UserDto();
        dto.setUsername("john_doe");
        dto.setEmail("john@example.com");
        dto.setPassword("secret"); // if UserDto has password

        User created = new User();
        created.setId(1L);
        created.setUsername("john_doe");
        created.setEmail("john@example.com");

        when(userService.createUser(dto)).thenReturn(created);

        ResponseEntity<User> response = controller.create(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(created, response.getBody());
        verify(userService, times(1)).createUser(dto);
    }

    @Test
    void update_withExistingUser_returns200AndUpdatedUser() {
        String existingUsername = "john_doe";
        UserDto dto = new UserDto();
        dto.setUsername("john_doe_updated");
        dto.setEmail("john_new@example.com");

        User updated = new User();
        updated.setId(1L);
        updated.setUsername("john_doe_updated");
        updated.setEmail("john_new@example.com");

        when(userService.updateUser(existingUsername, dto)).thenReturn(updated);

        ResponseEntity<User> response = controller.update(dto, existingUsername);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, response.getBody());
        verify(userService, times(1)).updateUser(existingUsername, dto);
    }

    @Test
    void update_whenUserNotFound_returns200WithNullBody() {
        String nonexistent = "no_user";
        UserDto dto = new UserDto();
        dto.setUsername("whatever");
        dto.setEmail("whatever@example.com");

        when(userService.updateUser(nonexistent, dto)).thenReturn(null);

        ResponseEntity<User> response = controller.update(dto, nonexistent);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).updateUser(nonexistent, dto);
    }

    @Test
    void delete_existingUsername_returns204() {
        String username = "to_delete";

        ResponseEntity<Void> response = controller.delete(username);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(username);
    }

    @Test
    void delete_nonexistentUsername_returns404() {
        String missing = "ghost";
        doThrow(new RuntimeException("User not found: " + missing))
                .when(userService).deleteUser(missing);

        ResponseEntity<Void> response = controller.delete(missing);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).deleteUser(missing);
    }

    @Test
    void get_whenUserExists_returns302Found() {
        Long id = 42L;
        User user = new User();
        user.setId(id);
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        when(userService.getUser(id)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = controller.get(id);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNull(response.getBody()); // Controller returns FOUND without body
        verify(userService, times(1)).getUser(id);
    }

    @Test
    void get_whenUserNotFound_returns204NoContent() {
        Long id = 99L;
        when(userService.getUser(id)).thenReturn(Optional.empty());

        ResponseEntity<User> response = controller.get(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).getUser(id);
    }

    @Test
    void fetchAllUsers_returns200AndDtoList() {
        UserDto dto1 = new UserDto();
        dto1.setUsername("u1");
        dto1.setEmail("u1@example.com");

        UserDto dto2 = new UserDto();
        dto2.setUsername("u2");
        dto2.setEmail("u2@example.com");

        List<UserDto> list = Arrays.asList(dto1, dto2);
        when(userService.fetchUsersDto()).thenReturn(list);

        ResponseEntity<List<UserDto>> response = controller.fetchAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());
        verify(userService, times(1)).fetchUsersDto();
    }
}

