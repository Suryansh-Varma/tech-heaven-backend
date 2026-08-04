package com.ansh.smart_commerce.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.dto.CheckoutResponse;
import com.ansh.smart_commerce.dto.OrderResponse;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.enums.PaymentMethod;
import com.ansh.smart_commerce.enums.PaymentStatus;
import com.ansh.smart_commerce.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void placeOrder_shouldReturnCreatedResponse() throws Exception {
        OrderResponse response = new OrderResponse(1L, java.time.LocalDateTime.now(), 1000, OrderStatus.PENDING, null, 1000, 0, 0, List.of());
        when(orderService.placeOrder(1L)).thenReturn(response);

        mockMvc.perform(post("/orders/place/1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(1));
    }

    @Test
    void getOrderHistory_shouldReturnOrders() throws Exception {
        when(orderService.getOrderHistory(1L)).thenReturn(List.of(new OrderResponse(1L, java.time.LocalDateTime.now(), 1000, OrderStatus.PENDING, null, 1000, 0, 0, List.of())));

        mockMvc.perform(get("/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].orderId").value(1));
    }

    @Test
    void cancelOrder_shouldDelegateToService() throws Exception {
        when(orderService.cancelOrder(2L)).thenReturn(new OrderResponse(2L, java.time.LocalDateTime.now(), 1000, OrderStatus.CANCELLED, null, 1000, 0, 0, List.of()));

        mockMvc.perform(patch("/orders/2/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void downloadInvoice_shouldReturnPdfHeaders() throws Exception {
        when(orderService.generateInvoicePdf(5L)).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/orders/5/invoice"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"invoice-5.pdf\""));
    }

    @Test
    void updateOrderStatus_shouldDelegateToService() throws Exception {
        when(orderService.updateOrderStatus(5L, "CANCELLED")).thenReturn(new OrderResponse(5L, java.time.LocalDateTime.now(), 1000, OrderStatus.CANCELLED, null, 1000, 0, 0, List.of()));

        mockMvc.perform(put("/orders/5/status").param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}