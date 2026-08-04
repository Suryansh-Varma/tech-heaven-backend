package com.ansh.smart_commerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ansh.smart_commerce.dto.PaymentResponse;
import com.ansh.smart_commerce.entity.Order;
import com.ansh.smart_commerce.entity.Payment;
import com.ansh.smart_commerce.enums.PaymentMethod;
import com.ansh.smart_commerce.enums.PaymentStatus;
import com.ansh.smart_commerce.exception.PaymentFailedException;
import com.ansh.smart_commerce.repository.OrderRepository;
import com.ansh.smart_commerce.repository.PaymentRepository;
import com.ansh.smart_commerce.service.razorpay.RazorpayGateway;
import com.ansh.smart_commerce.service.razorpay.RazorpayOrderResult;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RazorpayGateway razorpayGateway;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_shouldPersistPendingPaymentAndReturnResponse() {
        Order order = createOrder(42L);
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));

        Payment savedPayment = createPayment(order, 7L, PaymentStatus.PENDING, PaymentMethod.CARD, 150.0);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse response = paymentService.createPayment(42L, PaymentMethod.CARD, 150.0);

        assertNotNull(response);
        assertEquals(7L, response.getPaymentId());
        assertEquals(42L, response.getOrderId());
        assertEquals(PaymentMethod.CARD, response.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        assertEquals(150.0, response.getAmount(), 0.01);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void markSuccess_shouldUpdatePaymentToSuccess() {
        Payment payment = createPayment(createOrder(10L), 5L, PaymentStatus.PENDING, PaymentMethod.UPI, 99.0);
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.markSuccess(5L);

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals(5L, response.getPaymentId());
        verify(paymentRepository).save(payment);
    }

    @Test
    void markFailed_shouldUpdatePaymentToFailed() {
        Payment payment = createPayment(createOrder(11L), 6L, PaymentStatus.PENDING, PaymentMethod.COD, 75.0);
        when(paymentRepository.findById(6L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.markFailed(6L);

        assertEquals(PaymentStatus.FAILED, response.getPaymentStatus());
        assertEquals(6L, response.getPaymentId());
        verify(paymentRepository).save(payment);
    }

    @Test
    void getPaymentByOrder_shouldReturnPaymentForExistingOrder() {
        Order order = createOrder(20L);
        Payment payment = createPayment(order, 8L, PaymentStatus.PENDING, PaymentMethod.NET_BANKING, 200.0);
        when(orderRepository.findById(20L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByOrder(20L);

        assertEquals(8L, response.getPaymentId());
        assertEquals(20L, response.getOrderId());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
    }

    @Test
    void getPaymentByOrder_shouldThrowWhenNoPaymentExists() {
        Order order = createOrder(21L);
        when(orderRepository.findById(21L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());

        assertThrows(PaymentFailedException.class, () -> paymentService.getPaymentByOrder(21L));
    }

    @Test
    void createRazorpayOrder_shouldReuseExistingPendingPaymentForSameOrder() {
        Order order = createOrder(31L);
        ReflectionTestUtils.setField(order, "totalAmount", 250.0);
        Payment existingPayment = createPayment(order, 9L, PaymentStatus.PENDING, PaymentMethod.CARD, 250.0);
        when(orderRepository.findById(31L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(razorpayGateway.createOrder(25000L, "INR", "order-31")).thenReturn(
                new RazorpayOrderResult("rzp_test_reused", 25000L, "INR", "order-31"));

        var response = paymentService.createRazorpayOrder(31L, "INR");

        assertNotNull(response);
        assertEquals("rzp_test_reused", response.getOrderId());
        assertEquals(25000L, response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertEquals(PaymentStatus.PENDING, existingPayment.getPaymentStatus());
        verify(paymentRepository).save(existingPayment);
    }

    @Test
    void createRazorpayOrder_shouldCreatePaymentAndReturnRazorpayOrderDetails() {
        Order order = createOrder(30L);
        ReflectionTestUtils.setField(order, "totalAmount", 250.0);
        when(orderRepository.findById(30L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(razorpayGateway.createOrder(25000L, "INR", "order-30")).thenReturn(
                new RazorpayOrderResult("rzp_test_123", 25000L, "INR", "order-30"));

        var response = paymentService.createRazorpayOrder(30L, "INR");

        assertNotNull(response);
        assertEquals("rzp_test_123", response.getOrderId());
        assertEquals(25000L, response.getAmount());
        assertEquals("INR", response.getCurrency());
        verify(paymentRepository).save(any(Payment.class));
    }

    private Order createOrder(Long id) {
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    private Payment createPayment(Order order, Long id, PaymentStatus status, PaymentMethod method, double amount) {
        Payment payment = new Payment(order, method, status, "txn-" + id, amount, LocalDateTime.now());
        ReflectionTestUtils.setField(payment, "id", id);
        return payment;
    }
}
