package com.lawyer.belawyer.controllerTests;

import com.lawyer.belawyer.controller.AuthController;
import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.service.AuthenticationResponse;
import com.lawyer.belawyer.service.AuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Clear any existing Authentication in SecurityContext
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void logout_withoutAuthentication_returnsSuccessMessage() {
        // Arrange: no authentication set in SecurityContext

        // Act
        ResponseEntity<Map<String, String>> response = authController.logout(null);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Logged out successfully", body.get("message"));
        assertEquals("success", body.get("status"));
    }

    @Test
    void logout_withAuthenticatedUser_returnsSuccessMessage() {
        // Arrange: set a dummy authenticated principal
        Authentication auth = new UsernamePasswordAuthenticationToken("john_doe", null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Provide an Authorization header that starts with "Bearer "
        String authHeader = "Bearer dummy.token.value";

        // Act
        ResponseEntity<Map<String, String>> response = authController.logout(authHeader);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Logged out successfully", body.get("message"));
        assertEquals("success", body.get("status"));
        // Logging side‐effect is not verified here
    }

    @Test
    void register_delegatesToAuthenticationService_andReturnsSameResponseObject() {
        // Arrange
        UserDto dto = new UserDto();
        dto.setUsername("alice");
        dto.setEmail("alice@example.com");
        dto.setPassword("password123"); // assuming UserDto has a password field

        // Return a mock AuthenticationResponse (instead of calling a nonexistent constructor)
        AuthenticationResponse mockResponse = mock(AuthenticationResponse.class);
        when(authenticationService.register(dto)).thenReturn(mockResponse);

        // Act
        ResponseEntity<AuthenticationResponse> responseEntity = authController.register(dto);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        AuthenticationResponse body = responseEntity.getBody();
        assertSame(mockResponse, body);

        verify(authenticationService, times(1)).register(dto);
    }

    @Test
    void login_delegatesToAuthenticationService_andReturnsSameResponseObject() {
        // Arrange
        UserDto dto = new UserDto();
        dto.setUsername("bob");
        dto.setPassword("securePass"); // assuming UserDto has a password field

        AuthenticationResponse mockResponse = mock(AuthenticationResponse.class);
        when(authenticationService.authenticate(dto)).thenReturn(mockResponse);

        // Act
        ResponseEntity<AuthenticationResponse> responseEntity = authController.login(dto);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        AuthenticationResponse body = responseEntity.getBody();
        assertSame(mockResponse, body);

        verify(authenticationService, times(1)).authenticate(dto);
    }
}