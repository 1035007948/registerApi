package com.example.registerapi.controller;

import com.example.registerapi.dto.*;
import com.example.registerapi.security.JwtTokenProvider;
import com.example.registerapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求: {}", request.getUsername());
        UserResponse userResponse = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功", userResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求: {}", request.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        UserResponse userResponse = userService.getUserByUsername(request.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwt);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtTokenProvider.getExpirationTime());
        result.put("user", userResponse);

        return ResponseEntity.ok(ApiResponse.success("登录成功", result));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("退出登录成功", null));
    }
}
