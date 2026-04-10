package com.example.registerapi.controller;

import com.example.registerapi.dto.ApiResponse;
import com.example.registerapi.dto.RegisterRequest;
import com.example.registerapi.dto.UserResponse;
import com.example.registerapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("获取所有用户列表");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getCurrentUser().id == #id")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("获取用户信息: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.info("获取当前用户信息");
        UserResponse user = UserResponse.fromEntity(userService.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getCurrentUser().id == #id")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody RegisterRequest request) {
        log.info("更新用户信息: {}", id);
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("删除用户: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
