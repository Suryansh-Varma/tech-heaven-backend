package com.ansh.smart_commerce.service.razorpay;

public interface RazorpayGateway {
    RazorpayOrderResult createOrder(long amountInPaise, String currency, String receipt);
}
