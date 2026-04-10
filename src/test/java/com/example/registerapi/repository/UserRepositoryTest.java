package com.example.registerapi.repository;

import com.example.registerapi.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_WhenUserExists_ReturnsUser() {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> result = userRepository.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findByUsername_WhenUserNotExists_ReturnsEmpty() {
        Optional<User> result = userRepository.findByUsername("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_WhenUserExists_ReturnsUser() {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> result = userRepository.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void existsByUsername_WhenUsernameExists_ReturnsTrue() {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userRepository.existsByUsername("testuser");
        assertTrue(exists);
    }

    @Test
    void existsByUsername_WhenUsernameNotExists_ReturnsFalse() {
        boolean exists = userRepository.existsByUsername("nonexistent");
        assertFalse(exists);
    }

    @Test
    void existsByEmail_WhenEmailExists_ReturnsTrue() {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail("test@example.com");
        assertTrue(exists);
    }

    @Test
    void findByUsernameAndStatusNot_WhenUserNotDeleted_ReturnsUser() {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> result = userRepository.findByUsernameAndStatusNot("testuser", User.UserStatus.DELETED);

        assertTrue(result.isPresent());
    }

    @Test
    void findByUsernameAndStatusNot_WhenUserDeleted_ReturnsEmpty() {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .status(User.UserStatus.DELETED)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> result = userRepository.findByUsernameAndStatusNot("testuser", User.UserStatus.DELETED);

        assertFalse(result.isPresent());
    }

    @Test
    void saveAndFindById_Success() {
        User user = User.builder()
                .username("newuser")
                .password("password")
                .email("new@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        User savedUser = userRepository.save(user);
        
        Optional<User> result = userRepository.findById(savedUser.getId());
        
        assertTrue(result.isPresent());
        assertEquals("newuser", result.get().getUsername());
    }
}
