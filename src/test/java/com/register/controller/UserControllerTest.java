package com.register.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.register.dto.UserDTO;
import com.register.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO.UserResponse userResponse1;
    private UserDTO.UserResponse userResponse2;
    private UserDTO.RegisterRequest createRequest;
    private UserDTO.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        userResponse1 = new UserDTO.UserResponse();
        userResponse1.setId(1L);
        userResponse1.setUsername("user1");
        userResponse1.setEmail("user1@example.com");
        userResponse1.setPhone("13800138001");
        userResponse1.setCreateTime("2024-01-01 10:00:00");

        userResponse2 = new UserDTO.UserResponse();
        userResponse2.setId(2L);
        userResponse2.setUsername("user2");
        userResponse2.setEmail("user2@example.com");

        createRequest = new UserDTO.RegisterRequest();
        createRequest.setUsername("newuser");
        createRequest.setEmail("new@example.com");
        createRequest.setPassword("password123");

        updateRequest = new UserDTO.UpdateRequest();
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPhone("13999999999");
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse1);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("user1@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getAllUsers_ReturnsUserList() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(userResponse1, userResponse2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].username").value("user1"))
                .andExpect(jsonPath("$.data[1].username").value("user2"));

        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsers_WhenEmpty_ReturnsEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(userService).getAllUsers();
    }

    @Test
    void createUser_WhenValid_CreatesUser() throws Exception {
        when(userService.createUser(any(UserDTO.RegisterRequest.class))).thenReturn(userResponse1);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("user1"));

        verify(userService).createUser(any(UserDTO.RegisterRequest.class));
    }

    @Test
    void updateUser_WhenValid_UpdatesUser() throws Exception {
        UserDTO.UserResponse updatedResponse = new UserDTO.UserResponse();
        updatedResponse.setId(1L);
        updatedResponse.setUsername("user1");
        updatedResponse.setEmail("updated@example.com");
        updatedResponse.setPhone("13999999999");

        when(userService.updateUser(eq(1L), any(UserDTO.UpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.data.phone").value("13999999999"));

        verify(userService).updateUser(eq(1L), any(UserDTO.UpdateRequest.class));
    }

    @Test
    void deleteUser_WhenUserExists_DeletesSuccessfully() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).deleteUser(1L);
    }
}
