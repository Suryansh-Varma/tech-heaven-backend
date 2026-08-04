package com.ansh.smart_commerce.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ansh.smart_commerce.dto.CheckoutResponse;
import com.ansh.smart_commerce.enums.PaymentMethod;
import com.ansh.smart_commerce.enums.PaymentStatus;
import com.ansh.smart_commerce.service.OrderService;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CheckoutController checkoutController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(checkoutController).build();
    }

    @Test
    void checkout_shouldReturnCreatedResponse() throws Exception {
        when(orderService.checkout(org.mockito.ArgumentMatchers.any())).thenReturn(
                new CheckoutResponse(1L, 2L, PaymentMethod.CARD, PaymentStatus.PENDING, 1000, LocalDate.now().plusDays(5)));

        mockMvc.perform(post("/orders/checkout").contentType("application/json").content("{\"userId\":1,\"addressId\":1,\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderId").value(1));
    }
}