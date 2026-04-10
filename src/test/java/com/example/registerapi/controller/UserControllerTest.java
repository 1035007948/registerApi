package com.example.registerapi.controller;

import com.example.registerapi.dto.RegisterRequest;
import com.example.registerapi.dto.UserResponse;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private UserResponse userResponse;
    private List<UserResponse> userList;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phoneNumber("13800138000")
                .nickname("测试用户")
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        UserResponse user2 = UserResponse.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .nickname("管理员")
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        userList = Arrays.asList(userResponse, user2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success() throws Exception {
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @WithMockUser
    void getAllUsers_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser
    void getCurrentUser_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(new User());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void updateUser_Success() throws Exception {
        RegisterRequest updateRequest = RegisterRequest.builder()
                .email("newemail@example.com")
                .phoneNumber("13900139000")
                .nickname("新昵称")
                .password("")
                .confirmPassword("")
                .build();

        when(userService.updateUser(anyLong(), any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }
}
