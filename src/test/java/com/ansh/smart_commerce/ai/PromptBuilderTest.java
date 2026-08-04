package com.ansh.smart_commerce.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.dto.OrderItemResponse;
import com.ansh.smart_commerce.dto.OrderResponse;
import com.ansh.smart_commerce.entity.Coupon;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.enums.DiscountType;
import com.ansh.smart_commerce.enums.OrderStatus;

class PromptBuilderTest {

    @Test
    void buildSystemPrompt_shouldContainStorePolicies() {
        String prompt = PromptBuilder.buildSystemPrompt();

        assertTrue(prompt.contains("TechHeaven AI"));
        assertTrue(prompt.contains("Payment Methods"));
        assertTrue(prompt.contains("Coupons"));
    }

    @Test
    void buildProductSearchMessage_shouldHandleNoMatches() {
        String prompt = PromptBuilder.buildProductSearchMessage("find phones", List.of());

        assertTrue(prompt.contains("No products found"));
    }

    @Test
    void buildCouponMessage_shouldIncludeOnlyActiveCoupons() {
        Coupon active = TestFixtures.coupon(1L, "SAVE10", DiscountType.PERCENTAGE, 10, 500, LocalDate.now().plusDays(5), true);
        Coupon inactive = TestFixtures.coupon(2L, "OLD", DiscountType.FLAT, 100, 0, LocalDate.now().plusDays(5), false);

        String prompt = PromptBuilder.buildCouponMessage("what coupons are active", List.of(active, inactive));

        assertTrue(prompt.contains("SAVE10"));
        assertFalse(prompt.contains("OLD"));
    }

    @Test
    void buildSupportMessage_shouldRenderOrdersAndItems() {
        var user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(10L, "Laptop", 1000, 3, "Laptop");
        OrderResponse order = new OrderResponse(5L, LocalDateTime.now(), 1000, OrderStatus.CONFIRMED, null, 1000, 0, 0,
                List.of(new OrderItemResponse(product.getName(), 1, 1000, 1000, product.getId(), product.getImageUrl(), 1000)));

        String prompt = PromptBuilder.buildSupportMessage("where is my order", user, List.of(order), List.of());

        assertTrue(prompt.contains("Alice"));
        assertTrue(prompt.contains("Order #5"));
        assertTrue(prompt.contains("1x Laptop"));
    }
}