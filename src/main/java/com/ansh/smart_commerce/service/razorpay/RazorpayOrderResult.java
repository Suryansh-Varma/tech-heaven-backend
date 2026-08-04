package com.ansh.smart_commerce.service.razorpay;

public class RazorpayOrderResult {
    private final String orderId;
    private final long amount;
    private final String currency;
    private final String receipt;

    public RazorpayOrderResult(String orderId, long amount, String currency, String receipt) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.receipt = receipt;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getReceipt() {
        return receipt;
    }
}
