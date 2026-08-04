package com.ansh.smart_commerce.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.dto.LoginResponse;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.security.JwtService;
import com.ansh.smart_commerce.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void registerUser_shouldReturnCreatedUser() throws Exception {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        when(userService.registerUser(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(user);

        mockMvc.perform(post("/users/register").contentType("application/json").content("{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"));
    }

    @Test
    void login_shouldReturnJwtToken() throws Exception {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        when(userService.login("alice@example.com", "secret")).thenReturn(user);
        when(jwtService.generateToken("alice@example.com")).thenReturn("token-123");

        mockMvc.perform(post("/users/login").contentType("application/json").content("{\"email\":\"alice@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token-123"));
    }

    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(TestFixtures.user(1L, "Alice", "alice@example.com")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].email").value("alice@example.com"));
    }

    @Test
    void getUserById_shouldReturnSingleUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(TestFixtures.user(1L, "Alice", "alice@example.com"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Alice"));
    }
}