package com.ansh.smart_commerce.service.razorpay;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "razorpay.enabled", havingValue = "false", matchIfMissing = true)
public class RazorpayGatewayStub implements RazorpayGateway {

    @Override
    public RazorpayOrderResult createOrder(long amountInPaise, String currency, String receipt) {
        return new RazorpayOrderResult("rzp_test_stub_order", amountInPaise, currency, receipt);
    }
}
