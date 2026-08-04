package com.ansh.smart_commerce.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.dto.ai.ChatMessage;
import com.ansh.smart_commerce.dto.ai.ChatResponse;
import com.ansh.smart_commerce.entity.Coupon;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.enums.DiscountType;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.service.CouponService;
import com.ansh.smart_commerce.service.OrderService;
import com.ansh.smart_commerce.service.ProductService;


@ExtendWith(MockitoExtension.class)
class AiOrchestrationServicesTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductSearchService productSearchService;

    @Mock
    private GeminiService geminiService;

    @Mock
    private OrderService orderService;

    @Mock
    private CouponService couponService;

    @InjectMocks
    private ComparisonService comparisonService;

    @InjectMocks
    private RecommendationService recommendationService;

    @InjectMocks
    private SupportService supportService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void comparisonService_shouldReturnProductComparison() {
        Product left = TestFixtures.product(1L, "iPhone 16", 99999, 5, "Phones");
        Product right = TestFixtures.product(2L, "Samsung S25", 89999, 4, "Phones");

        when(productService.getAllProducts()).thenReturn(List.of(left, right));
        when(productSearchService.findByName(List.of(left, right), "iphone 16")).thenReturn(java.util.Optional.of(left));
        when(productSearchService.findByName(List.of(left, right), "samsung s25")).thenReturn(java.util.Optional.of(right));
        when(geminiService.generateContent(any(), any(), any())).thenReturn("comparison");

        ChatResponse response = comparisonService.compare("compare iphone 16 vs samsung s25", List.of(new ChatMessage("user", "hi")));

        assertEquals(ChatResponse.ResponseType.PRODUCTS, response.getResponseType());
        assertEquals(2, response.getProducts().size());
    }

    @Test
    void recommendationService_shouldReturnAccessorySuggestions() {
        Product base = TestFixtures.product(1L, "Laptop", 50000, 5, "Laptops");
        Product accessory = TestFixtures.product(2L, "Mouse", 1000, 10, "Accessories");

        when(productService.getAllProducts()).thenReturn(List.of(base, accessory));
        when(productSearchService.findByName(List.of(base, accessory), "laptop")).thenReturn(java.util.Optional.of(base));
        when(productSearchService.findAccessories(List.of(base, accessory), base)).thenReturn(List.of(accessory));
        when(geminiService.generateContent(any(), any(), any())).thenReturn("recommendation");

        ChatResponse response = recommendationService.recommend("i bought a laptop", List.of());

        assertEquals(ChatResponse.ResponseType.PRODUCTS, response.getResponseType());
        assertEquals(1, response.getProducts().size());
        assertEquals("Mouse", response.getProducts().get(0).getName());
    }

    @Test
    void supportService_shouldAttachLatestOrderForOrderQueries() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 50000, 5, "Laptops");
        var order = TestFixtures.order(10L, user, OrderStatus.PENDING);
        order.setOrderItems(List.of(TestFixtures.orderItem(order, product, 1, 50000)));

        when(orderService.getOrderHistory(1L)).thenReturn(List.of(new com.ansh.smart_commerce.dto.OrderResponse(10L, java.time.LocalDateTime.now(), 50000, OrderStatus.PENDING, null, 50000, 0, 0, List.of())));
        when(couponService.getAllCoupons()).thenReturn(List.of(TestFixtures.coupon(1L, "SAVE10", DiscountType.PERCENTAGE, 10, 500, java.time.LocalDate.now().plusDays(5), true)));
        when(geminiService.generateContent(any(), any(), any())).thenReturn("support");

        ChatResponse response = supportService.handleSupport("where is my order", user, List.of());

        assertEquals(ChatResponse.ResponseType.ORDER, response.getResponseType());
        assertNotNull(response.getOrderInfo());
        assertEquals(10L, response.getOrderInfo().getOrderId());
    }

    @Test
    void chatService_shouldPromptSignInForPublicOrderSupport() {
        com.ansh.smart_commerce.dto.ai.ChatRequest request = new com.ansh.smart_commerce.dto.ai.ChatRequest();
        request.setMessage("where is my order");

        ChatResponse response = chatService.chatPublic(request);

        assertEquals(ChatResponse.ResponseType.TEXT, response.getResponseType());
        assertEquals("To track your order or get personalized support, please **sign in** first. I can then access your real order history and give you accurate updates.", response.getMessage());
        assertEquals(ChatResponse.ResponseType.TEXT, response.getResponseType());
    }
}