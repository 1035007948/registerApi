package com.example.registerapi.controller;

import com.example.registerapi.dto.UserRegistrationDto;
import com.example.registerapi.model.User;
import com.example.registerapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void whenRegisterUser_thenReturnCreated() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "newuser",
                "password123",
                "newuser@example.com"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("用户注册成功"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void whenRegisterDuplicateUsername_thenReturnBadRequest() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "testuser",
                "password123",
                "another@example.com"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenLoginWithValidCredentials_thenReturnJwtToken() throws Exception {
        String loginJson = "{\"username\":\"testuser\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void whenLoginWithInvalidCredentials_thenReturnBadRequest() throws Exception {
        String loginJson = "{\"username\":\"testuser\",\"password\":\"wrongpassword\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetAllUsers_withoutAuth_thenReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenRegisterWithInvalidEmail_thenReturnBadRequest() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "validuser",
                "password123",
                "invalid-email"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenRegisterWithShortPassword_thenReturnBadRequest() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "validuser",
                "123",
                "valid@example.com"
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());
    }
}
