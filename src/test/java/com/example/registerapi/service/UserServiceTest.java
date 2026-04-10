package com.example.registerapi.service;

import com.example.registerapi.dto.RegisterRequest;
import com.example.registerapi.dto.UserResponse;
import com.example.registerapi.entity.User;
import com.example.registerapi.exception.BusinessException;
import com.example.registerapi.repository.UserRepository;
import com.example.registerapi.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .phoneNumber("13800138000")
                .nickname("测试用户")
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .confirmPassword("password123")
                .email("test@example.com")
                .phoneNumber("13800138000")
                .nickname("测试用户")
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.register(registerRequest);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_PasswordNotMatch_ThrowsException() {
        registerRequest.setConfirmPassword("differentPassword");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("两次输入的密码不一致", exception.getMessage());
    }

    @Test
    void register_UsernameExists_ThrowsException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    void register_EmailExists_ThrowsException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("邮箱已被注册", exception.getMessage());
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getUserById(1L);
        });

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void getUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getAllUsers_Success() {
        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RegisterRequest updateRequest = RegisterRequest.builder()
                .email("newemail@example.com")
                .phoneNumber("13900139000")
                .nickname("新昵称")
                .password("")
                .confirmPassword("")
                .build();

        UserResponse result = userService.updateUser(1L, updateRequest);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deleteUser(1L);

        assertEquals(User.UserStatus.DELETED, testUser.getStatus());
        verify(userRepository).save(testUser);
    }
}
