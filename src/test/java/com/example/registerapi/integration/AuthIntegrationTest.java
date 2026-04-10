package com.example.registerapi.integration;

import com.example.registerapi.dto.LoginRequest;
import com.example.registerapi.dto.RegisterRequest;
import com.example.registerapi.dto.UserResponse;
import com.example.registerapi.entity.User;
import com.example.registerapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_IntegrationTest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .confirmPassword("password123")
                .email("newuser@example.com")
                .nickname("新用户")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("newuser"));
    }

    @Test
    void register_PasswordNotMatch_IntegrationTest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .confirmPassword("different")
                .email("newuser@example.com")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("两次输入的密码不一致"));
    }

    @Test
    void register_DuplicateUsername_IntegrationTest() throws Exception {
        User existingUser = User.builder()
                .username("existinguser")
                .password(passwordEncoder.encode("password123"))
                .email("existing@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .password("password123")
                .confirmPassword("password123")
                .email("new@example.com")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void login_IntegrationTest() throws Exception {
        User user = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }

    @Test
    void login_WrongPassword_IntegrationTest() throws Exception {
        User user = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_UserNotFound_IntegrationTest() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
