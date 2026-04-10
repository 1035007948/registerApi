package com.example.registerapi.service;

import com.example.registerapi.dto.RegisterRequest;
import com.example.registerapi.dto.UserResponse;
import com.example.registerapi.entity.User;

import java.util.List;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, RegisterRequest request);

    void deleteUser(Long id);

    User getCurrentUser();
}
