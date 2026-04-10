package com.example.registerapi.service;

import com.example.registerapi.dto.UserRegistrationDto;
import com.example.registerapi.dto.UserUpdateDto;
import com.example.registerapi.model.User;
import com.example.registerapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void whenRegisterUser_thenReturnSavedUser() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "testuser",
                "password123",
                "test@example.com"
        );

        User savedUser = userService.registerUser(dto);

        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void whenRegisterDuplicateUsername_thenThrowException() {
        UserRegistrationDto dto1 = new UserRegistrationDto(
                "testuser",
                "password123",
                "test1@example.com"
        );
        userService.registerUser(dto1);

        UserRegistrationDto dto2 = new UserRegistrationDto(
                "testuser",
                "password456",
                "test2@example.com"
        );

        assertThrows(RuntimeException.class, () -> userService.registerUser(dto2));
    }

    @Test
    void whenRegisterDuplicateEmail_thenThrowException() {
        UserRegistrationDto dto1 = new UserRegistrationDto(
                "user1",
                "password123",
                "test@example.com"
        );
        userService.registerUser(dto1);

        UserRegistrationDto dto2 = new UserRegistrationDto(
                "user2",
                "password456",
                "test@example.com"
        );

        assertThrows(RuntimeException.class, () -> userService.registerUser(dto2));
    }

    @Test
    void whenFindById_thenReturnUser() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "testuser",
                "password123",
                "test@example.com"
        );
        User savedUser = userService.registerUser(dto);

        Optional<User> foundUser = userService.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void whenFindByUsername_thenReturnUser() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "testuser",
                "password123",
                "test@example.com"
        );
        userService.registerUser(dto);

        Optional<User> foundUser = userService.findByUsername("testuser");

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void whenFindAll_thenReturnAllUsers() {
        UserRegistrationDto dto1 = new UserRegistrationDto(
                "user1",
                "password123",
                "user1@example.com"
        );
        UserRegistrationDto dto2 = new UserRegistrationDto(
                "user2",
                "password456",
                "user2@example.com"
        );
        userService.registerUser(dto1);
        userService.registerUser(dto2);

        List<User> users = userService.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void whenUpdateUser_thenReturnUpdatedUser() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "testuser",
                "password123",
                "test@example.com"
        );
        User savedUser = userService.registerUser(dto);

        UserUpdateDto updateDto = new UserUpdateDto("newemail@example.com");
        User updatedUser = userService.updateUser(savedUser.getId(), updateDto);

        assertEquals("newemail@example.com", updatedUser.getEmail());
    }

    @Test
    void whenDeleteUser_thenUserIsDeleted() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "testuser",
                "password123",
                "test@example.com"
        );
        User savedUser = userService.registerUser(dto);

        userService.deleteUser(savedUser.getId());

        Optional<User> deletedUser = userService.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void whenDeleteNonExistentUser_thenThrowException() {
        assertThrows(RuntimeException.class, () -> userService.deleteUser(999L));
    }
}
