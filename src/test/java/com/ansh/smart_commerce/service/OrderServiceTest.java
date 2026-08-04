package com.ansh.smart_commerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.dto.CheckoutRequest;
import com.ansh.smart_commerce.dto.CheckoutResponse;
import com.ansh.smart_commerce.dto.OrderResponse;
import com.ansh.smart_commerce.entity.Address;
import com.ansh.smart_commerce.entity.CartItem;
import com.ansh.smart_commerce.entity.Order;
import com.ansh.smart_commerce.entity.Payment;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.enums.PaymentMethod;
import com.ansh.smart_commerce.enums.PaymentStatus;
import com.ansh.smart_commerce.exception.CartEmptyException;
import com.ansh.smart_commerce.exception.InsufficientStockException;
import com.ansh.smart_commerce.exception.OrderNotFoundException;
import com.ansh.smart_commerce.repository.AddressRepository;
import com.ansh.smart_commerce.repository.CartRepository;
import com.ansh.smart_commerce.repository.OrderRepository;
import com.ansh.smart_commerce.repository.PaymentRepository;
import com.ansh.smart_commerce.repository.ProductRepository;
import com.ansh.smart_commerce.security.SecurityHelper;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private CouponService couponService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void checkout_shouldCreateOrderAndPayment() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 50000, 10, "Laptops");
        CartItem cartItem = TestFixtures.cartItem(11L, user, product, 1);
        Address address = TestFixtures.address(20L, user, true);

        CheckoutRequest request = buildCheckoutRequest(address.getId(), PaymentMethod.CARD, null);

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(cartRepository.findByUser(user)).thenReturn(List.of(cartItem));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(order, "id", 99L);
            return order;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(payment, "id", 77L);
            return payment;
        });

        CheckoutResponse response = orderService.checkout(request);

        assertEquals(99L, response.getOrderId());
        assertEquals(77L, response.getPaymentId());
        assertEquals(PaymentMethod.CARD, response.getPaymentMethod());
    }

    @Test
    void checkout_shouldRejectEmptyCart() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        CheckoutRequest request = buildCheckoutRequest(20L, PaymentMethod.UPI, null);

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(addressRepository.findById(20L)).thenReturn(Optional.of(TestFixtures.address(20L, user, true)));
        when(cartRepository.findByUser(user)).thenReturn(List.of());

        assertThrows(CartEmptyException.class, () -> orderService.checkout(request));
    }

    @Test
    void checkout_shouldRejectInsufficientStock() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 50000, 0, "Laptops");
        CartItem cartItem = TestFixtures.cartItem(11L, user, product, 1);
        CheckoutRequest request = buildCheckoutRequest(20L, PaymentMethod.UPI, null);

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(addressRepository.findById(20L)).thenReturn(Optional.of(TestFixtures.address(20L, user, true)));
        when(cartRepository.findByUser(user)).thenReturn(List.of(cartItem));

        assertThrows(InsufficientStockException.class, () -> orderService.checkout(request));
    }

    @Test
    void placeOrder_shouldUseDefaultAddressWhenAvailable() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 50000, 10, "Laptops");
        CartItem cartItem = TestFixtures.cartItem(11L, user, product, 1);
        Address address = TestFixtures.address(20L, user, true);

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(List.of(cartItem));
        when(addressRepository.findByUser(user)).thenReturn(List.of(address));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(order, "id", 100L);
            return order;
        });

        OrderResponse response = orderService.placeOrder(user.getId());

        assertEquals(100L, response.getOrderId());
        assertEquals("Alice", response.getShippingName());
    }

    @Test
    void getOrderHistory_shouldReturnAllOrdersForRoot() {
        User root = TestFixtures.user(9999L, "Root", "root@techheaven.com");
        Order order = TestFixtures.order(10L, TestFixtures.user(1L, "Alice", "alice@example.com"), OrderStatus.CONFIRMED);

        when(securityHelper.getCurrentUser()).thenReturn(root);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        assertEquals(1, orderService.getOrderHistory(root.getId()).size());
    }

    @Test
    void getOrderById_shouldRejectForeignOrderForNormalUser() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Order order = TestFixtures.order(10L, TestFixtures.user(2L, "Bob", "bob@example.com"), OrderStatus.CONFIRMED);

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderById(10L));
    }

    @Test
    void cancelOrder_shouldRestoreStockAndMarkCancelled() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 50000, 5, "Laptops");
        Order order = TestFixtures.order(10L, user, OrderStatus.PENDING);
        order.setOrderItems(List.of(TestFixtures.orderItem(order, product, 2, 50000)));

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(10L);

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertEquals(7, product.getStock());
    }

    @Test
    void updateOrderStatus_shouldAllowRootAndRestoreStockOnCancel() {
        User root = TestFixtures.user(9999L, "Root", "root@techheaven.com");
        Product product = TestFixtures.product(2L, "Laptop", 50000, 5, "Laptops");
        Order order = TestFixtures.order(10L, TestFixtures.user(1L, "Alice", "alice@example.com"), OrderStatus.PENDING);
        order.setOrderItems(List.of(TestFixtures.orderItem(order, product, 1, 50000)));

        when(securityHelper.getCurrentUser()).thenReturn(root);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse response = orderService.updateOrderStatus(10L, "CANCELLED");

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertEquals(6, product.getStock());
    }

    @Test
    void generateInvoicePdf_shouldReturnBytes() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 50000, 10, "Laptops");
        Order order = TestFixtures.order(10L, user, OrderStatus.CONFIRMED);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(50000);
        order.setSubtotal(50000);
        order.setServiceFee(90);
        order.setOrderItems(List.of(TestFixtures.orderItem(order, product, 1, 50000)));
        Payment payment = TestFixtures.payment(20L, order, PaymentMethod.CARD, PaymentStatus.SUCCESS, "txn-1", 50090);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        byte[] pdf = orderService.generateInvoicePdf(10L);

        assertNotNull(pdf);
        assertEquals(true, pdf.length > 0);
    }

    private CheckoutRequest buildCheckoutRequest(Long addressId, PaymentMethod paymentMethod, String couponCode) {
        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(addressId);
        request.setPaymentMethod(paymentMethod);
        request.setCouponCode(couponCode);
        return request;
    }
}