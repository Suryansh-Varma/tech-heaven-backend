package com.ansh.smart_commerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.dto.PaymentResponse;
import com.ansh.smart_commerce.service.PaymentService;
import com.ansh.smart_commerce.service.razorpay.RazorpayOrderResult;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/success/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> markSuccess(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment successful", paymentService.markSuccess(paymentId)));
    }

    @PostMapping("/failure/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> markFailed(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment marked as failed", paymentService.markFailed(paymentId)));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment retrieved", paymentService.getPaymentById(paymentId)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment retrieved", paymentService.getPaymentByOrder(orderId)));
    }

    @PostMapping("/razorpay/order/{orderId}")
    public ResponseEntity<ApiResponse<RazorpayOrderResult>> createRazorpayOrder(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "INR") String currency) {
        return ResponseEntity.ok(
                ApiResponse.success("Razorpay order created", paymentService.createRazorpayOrder(orderId, currency)));
    }
}
