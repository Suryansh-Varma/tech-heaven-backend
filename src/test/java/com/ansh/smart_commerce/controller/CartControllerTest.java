package com.ansh.smart_commerce.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.ansh.smart_commerce.dto.CartResponse;
import com.ansh.smart_commerce.dto.UpdateQuantityRequest;
import com.ansh.smart_commerce.security.SecurityHelper;
import com.ansh.smart_commerce.service.CartService;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    void addToCart_shouldUseCurrentUserId() throws Exception {
        when(securityHelper.getCurrentUser()).thenReturn(TestFixtures.user(1L, "Alice", "alice@example.com"));
        when(cartService.addToCart(org.mockito.ArgumentMatchers.any())).thenReturn(new CartResponse(1L, 2L, "Laptop", "img", 50000, 1, 50000));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/cart/add")
                        .contentType("application/json")
                        .content("{\"userId\":999,\"productId\":2,\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(2));
    }

    @Test
    void getCart_shouldIgnorePathUserIdAndUseCurrentUser() throws Exception {
        when(securityHelper.getCurrentUser()).thenReturn(TestFixtures.user(1L, "Alice", "alice@example.com"));
        when(cartService.getCart(1L)).thenReturn(List.of(new CartResponse(1L, 2L, "Laptop", "img", 50000, 1, 50000)));

        mockMvc.perform(get("/cart/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].productName").value("Laptop"));

        verify(cartService).getCart(1L);
    }

    @Test
    void updateQuantity_shouldDelegateToService() throws Exception {
        mockMvc.perform(put("/cart/7").contentType("application/json").content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService).updateQuantity(7L, 3);
    }

    @Test
    void removeItem_shouldDelegateToService() throws Exception {
        mockMvc.perform(delete("/cart/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService).removeItem(7L);
    }
}