package com.register.controller;

import com.register.common.Result;
import com.register.dto.UserDTO;
import com.register.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Result<UserDTO.UserResponse> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping
    public Result<List<UserDTO.UserResponse>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @PostMapping
    public Result<UserDTO.UserResponse> createUser(@Valid @RequestBody UserDTO.RegisterRequest request) {
        return Result.success(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public Result<UserDTO.UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UserDTO.UpdateRequest request) {
        return Result.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
