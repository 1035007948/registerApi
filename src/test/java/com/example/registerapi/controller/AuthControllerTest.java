package com.example.registerapi.controller;

import com.example.registerapi.dto.*;
import com.example.registerapi.entity.User;
import com.example.registerapi.security.JwtTokenProvider;
import com.example.registerapi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .confirmPassword("password123")
                .email("test@example.com")
                .phoneNumber("13800138000")
                .nickname("测试用户")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phoneNumber("13800138000")
                .nickname("测试用户")
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_Success() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void register_ValidationError() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("ab")
                .password("123")
                .confirmPassword("123")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("test-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);
        when(userService.getUserByUsername("testuser")).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.token").value("test-token"));
    }

    @Test
    void logout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("退出登录成功"));
    }
}
