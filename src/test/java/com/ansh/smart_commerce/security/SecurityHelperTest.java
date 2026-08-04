package com.ansh.smart_commerce.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.repository.UserRepository;

class SecurityHelperTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final SecurityHelper securityHelper = new SecurityHelper(userRepository);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_shouldReturnUserFromRepository() {
        User user = TestFixtures.user(10L, "Alice", "alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        setAuthentication("alice@example.com", true);

        User resolved = securityHelper.getCurrentUser();

        assertEquals(10L, resolved.getId());
        assertEquals("alice@example.com", resolved.getEmail());
    }

    @Test
    void getCurrentUser_shouldReturnSyntheticRootUser() {
        setAuthentication("root@techheaven.com", true);

        User resolved = securityHelper.getCurrentUser();

        assertEquals(9999L, resolved.getId());
        assertEquals("root@techheaven.com", resolved.getEmail());
        assertEquals("Root Administrator", resolved.getName());
    }

    @Test
    void getCurrentUser_shouldFailWhenUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertThrows(IllegalStateException.class, securityHelper::getCurrentUser);
    }

    private void setAuthentication(String email, boolean authenticated) {
        Authentication authentication = authenticated
                ? new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("USER")))
                : new UsernamePasswordAuthenticationToken(email, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}