package com.example.registerapi.service;

import com.example.registerapi.dto.UserRegistrationDto;
import com.example.registerapi.dto.UserUpdateDto;
import com.example.registerapi.model.User;
import com.example.registerapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }

        User user = new User(
                registrationDto.getUsername(),
                passwordEncoder.encode(registrationDto.getPassword()),
                registrationDto.getEmail()
        );

        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, UserUpdateDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (updateDto.getEmail() != null && !updateDto.getEmail().isEmpty()) {
            if (!user.getEmail().equals(updateDto.getEmail()) && 
                userRepository.existsByEmail(updateDto.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }
            user.setEmail(updateDto.getEmail());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }
}
