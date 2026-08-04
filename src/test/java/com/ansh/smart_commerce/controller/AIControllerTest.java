package com.ansh.smart_commerce.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.ai.ChatService;
import com.ansh.smart_commerce.dto.ai.ChatRequest;
import com.ansh.smart_commerce.dto.ai.ChatResponse;
import com.ansh.smart_commerce.security.SecurityHelper;

@ExtendWith(MockitoExtension.class)
class AIControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private AIController aiController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
    }

    @Test
    void chat_shouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/ai/chat").contentType("application/json").content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void chat_shouldReturnAiResponseForAuthenticatedUser() throws Exception {
        when(securityHelper.getCurrentUser()).thenReturn(TestFixtures.user(1L, "Alice", "alice@example.com"));
        when(chatService.chat(org.mockito.ArgumentMatchers.any(ChatRequest.class), org.mockito.ArgumentMatchers.any())).thenReturn(
                ChatResponse.textOnly("Hello", java.util.List.of("Browse products")));

        mockMvc.perform(post("/ai/chat").contentType("application/json").content("{\"message\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Hello"));
    }
}