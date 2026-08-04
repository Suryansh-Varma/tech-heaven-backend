package com.ansh.smart_commerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_shouldLoadRegularUser() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        user.setPassword("encoded");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        var userDetails = customUserDetailsService.loadUserByUsername("alice@example.com");

        assertEquals("alice@example.com", userDetails.getUsername());
        assertEquals("encoded", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_shouldLoadRootAccount() {
        var userDetails = customUserDetailsService.loadUserByUsername("root");

        assertEquals("root@techheaven.com", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrowWhenMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@example.com"));
    }
}