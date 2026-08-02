package com.legacyvault.legacyvault;

import com.legacyvault.legacyvault.model.User;
import com.legacyvault.legacyvault.repository.UserRepository;
import com.legacyvault.legacyvault.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);

        userService = new UserService(userRepository);
    }


    // TEST 1
    // Registration should hash the password before saving
    @Test
    void shouldRegisterUserWithHashedPassword() {

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.registerUser(
                "Test User",
                "test@example.com",
                "Password123!"
        );

        assertNotNull(user);

        assertEquals(
                "Test User",
                user.getName()
        );

        assertEquals(
                "test@example.com",
                user.getEmail()
        );

        assertNotEquals(
                "Password123!",
                user.getPasswordHash()
        );

        assertTrue(
                user.getPasswordHash().startsWith("$2")
        );

        verify(userRepository, times(1))
                .save(any(User.class));
    }


    // TEST 2
    // Duplicate email must be rejected
    @Test
    void shouldRejectDuplicateEmail() {

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> userService.registerUser(
                                "Test User",
                                "test@example.com",
                                "Password123!"
                        )
                );

        assertEquals(
                409,
                exception.getStatusCode().value()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    // TEST 3
    // Correct password should allow login
    @Test
    void shouldLoginWithCorrectPassword() {

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = userService.registerUser(
                "Test User",
                "test@example.com",
                "Password123!"
        );

        assertNotNull(registeredUser);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(registeredUser));

        User loggedInUser = userService.loginUser(
                "test@example.com",
                "Password123!"
        );

        assertNotNull(loggedInUser);

        assertEquals(
                "test@example.com",
                loggedInUser.getEmail()
        );
    }


    // TEST 4
    // Wrong password must be rejected
    @Test
    void shouldRejectIncorrectPassword() {

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = userService.registerUser(
                "Test User",
                "test@example.com",
                "CorrectPassword123!"
        );

        assertNotNull(registeredUser);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(registeredUser));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> userService.loginUser(
                                "test@example.com",
                                "WrongPassword"
                        )
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );
    }


    // TEST 5
    // Unknown email must be rejected
    @Test
    void shouldRejectUnknownEmail() {

        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> userService.loginUser(
                                "missing@example.com",
                                "Password123!"
                        )
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );
    }
}