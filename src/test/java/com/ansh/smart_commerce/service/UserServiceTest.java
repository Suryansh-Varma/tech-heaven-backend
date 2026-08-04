package com.ansh.smart_commerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.exception.EmailAlreadyExistsException;
import com.ansh.smart_commerce.exception.InvalidCredentialsException;
import com.ansh.smart_commerce.exception.UserNotFoundException;
import com.ansh.smart_commerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_shouldEncodePasswordAndSave() {
        User user = TestFixtures.user(0L, "Alice", "alice@example.com");
        user.setPassword("plain-text");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-text")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.registerUser(user);

        assertEquals("encoded", saved.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void registerUser_shouldRejectDuplicateEmail() {
        User user = TestFixtures.user(0L, "Alice", "alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(user));
    }

    @Test
    void login_shouldAuthenticateRegularUser() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        user.setPassword("encoded");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        User resolved = userService.login("alice@example.com", "secret");

        assertEquals(1L, resolved.getId());
    }

    @Test
    void login_shouldRejectInvalidPassword() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        user.setPassword("encoded");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login("alice@example.com", "wrong"));
    }

    @Test
    void getUserById_shouldReturnFoundUser() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertEquals("Alice", userService.getUserById(1L).getName());
    }

    @Test
    void getAllUsers_shouldReturnRepositoryResult() {
        when(userRepository.findAll()).thenReturn(List.of(TestFixtures.user(1L, "Alice", "alice@example.com")));

        assertEquals(1, userService.getAllUsers().size());
    }

    @Test
    void getUserById_shouldThrowWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }
}