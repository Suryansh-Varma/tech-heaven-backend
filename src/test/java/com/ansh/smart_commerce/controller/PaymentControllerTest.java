package com.ansh.smart_commerce.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ansh.smart_commerce.dto.PaymentResponse;
import com.ansh.smart_commerce.enums.PaymentMethod;
import com.ansh.smart_commerce.enums.PaymentStatus;
import com.ansh.smart_commerce.service.PaymentService;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    void markSuccess_shouldReturnPaymentResponse() throws Exception {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentStatus(PaymentStatus.SUCCESS);
        response.setPaymentMethod(PaymentMethod.CARD);
        when(paymentService.markSuccess(7L)).thenReturn(response);

        mockMvc.perform(post("/payments/success/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentStatus").value("SUCCESS"));
    }

    @Test
    void markFailed_shouldReturnPaymentResponse() throws Exception {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentStatus(PaymentStatus.FAILED);
        response.setPaymentMethod(PaymentMethod.UPI);
        when(paymentService.markFailed(8L)).thenReturn(response);

        mockMvc.perform(post("/payments/failure/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentStatus").value("FAILED"));
    }

    @Test
    void getPaymentByOrder_shouldReturnPaymentResponse() throws Exception {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(11L);
        response.setOrderId(22L);
        response.setPaymentMethod(PaymentMethod.NET_BANKING);
        response.setPaymentStatus(PaymentStatus.PENDING);
        when(paymentService.getPaymentByOrder(22L)).thenReturn(response);

        mockMvc.perform(get("/payments/order/22").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentId").value(11));
    }

    @Test
    void getPayment_shouldReturnPaymentResponse() throws Exception {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(15L);
        response.setOrderId(25L);
        response.setPaymentMethod(PaymentMethod.CARD);
        response.setPaymentStatus(PaymentStatus.SUCCESS);
        when(paymentService.getPaymentById(15L)).thenReturn(response);

        mockMvc.perform(get("/payments/15").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentStatus").value("SUCCESS"));
    }

    @Test
    void createRazorpayOrder_shouldReturnGatewayResult() throws Exception {
        when(paymentService.createRazorpayOrder(33L, "INR"))
                .thenReturn(new com.ansh.smart_commerce.service.razorpay.RazorpayOrderResult("rzp_order_1", 1000L, "INR", "order-33"));

        mockMvc.perform(post("/payments/razorpay/order/33").param("currency", "INR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("rzp_order_1"));
    }
}
