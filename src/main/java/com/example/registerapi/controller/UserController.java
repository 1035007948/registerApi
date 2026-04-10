package com.example.registerapi.controller;

import com.example.registerapi.dto.JwtResponse;
import com.example.registerapi.dto.LoginDto;
import com.example.registerapi.dto.UserRegistrationDto;
import com.example.registerapi.dto.UserUpdateDto;
import com.example.registerapi.model.User;
import com.example.registerapi.service.AuthService;
import com.example.registerapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        User user = userService.registerUser(registrationDto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "用户注册成功");
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        JwtResponse jwtResponse = authService.authenticateUser(loginDto);
        return ResponseEntity.ok(jwtResponse);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        
        if (!user.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "用户不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        return ResponseEntity.ok(user.get());
    }

    @GetMapping("/users/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> user = userService.findByUsername(userDetails.getUsername());
        
        if (!user.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "用户不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        return ResponseEntity.ok(user.get());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @Valid @RequestBody UserUpdateDto updateDto,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        
        if (!currentUser.isPresent() || !currentUser.get().getId().equals(id)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "无权限修改此用户信息");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        User updatedUser = userService.updateUser(id, updateDto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "用户信息更新成功");
        response.put("user", updatedUser);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        
        if (!currentUser.isPresent() || !currentUser.get().getId().equals(id)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "无权限删除此用户");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        userService.deleteUser(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "用户删除成功");
        
        return ResponseEntity.ok(response);
    }
}
