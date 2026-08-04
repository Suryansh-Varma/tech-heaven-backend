package com.ansh.smart_commerce.service.razorpay;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Component
@ConditionalOnProperty(name = "razorpay.enabled", havingValue = "true")
public class RazorpayGatewayImpl implements RazorpayGateway {

    private final RazorpayClient client;

    public RazorpayGatewayImpl(@Value("${razorpay.key_id}") String keyId,
                               @Value("${razorpay.key_secret}") String keySecret) {
        try {
            this.client = new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException ex) {
            throw new IllegalStateException("Unable to initialize Razorpay client", ex);
        }
    }

    @Override
    public RazorpayOrderResult createOrder(long amountInPaise, String currency, String receipt) {
        try {
            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", currency);
            options.put("receipt", receipt);

            com.razorpay.Order order = client.orders.create(options);
            org.json.JSONObject payload = order.toJson();
            return new RazorpayOrderResult(
                    payload.getString("id"),
                    payload.getInt("amount"),
                    payload.getString("currency"),
                    payload.optString("receipt", "")
            );
        } catch (RazorpayException ex) {
            throw new IllegalStateException("Failed to create Razorpay order", ex);
        }
    }
}
