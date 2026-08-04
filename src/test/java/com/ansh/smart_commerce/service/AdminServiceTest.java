package com.ansh.smart_commerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.dto.DashboardResponse;
import com.ansh.smart_commerce.entity.Order;
import com.ansh.smart_commerce.entity.OrderItem;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.repository.OrderRepository;
import com.ansh.smart_commerce.repository.ProductRepository;
import com.ansh.smart_commerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getDashboard_shouldAggregateTotals() {
        Product lowStock = TestFixtures.product(1L, "Mouse", 1000, 4, "Accessories");
        Product normalStock = TestFixtures.product(2L, "Laptop", 50000, 10, "Laptops");

        Order confirmed = TestFixtures.order(10L, TestFixtures.user(1L, "Alice", "alice@example.com"), OrderStatus.CONFIRMED);
        confirmed.setTotalAmount(50000);
        confirmed.setOrderItems(List.of(new OrderItem(confirmed, lowStock, 2, 1000)));

        Order pending = TestFixtures.order(11L, TestFixtures.user(2L, "Bob", "bob@example.com"), OrderStatus.PENDING);
        pending.setOrderItems(List.of(new OrderItem(pending, normalStock, 1, 50000)));

        when(userRepository.count()).thenReturn(5L);
        when(orderRepository.count()).thenReturn(2L);
        when(orderRepository.findAll()).thenReturn(List.of(confirmed, pending));
        when(productRepository.findAll()).thenReturn(List.of(lowStock, normalStock));

        DashboardResponse response = adminService.getDashboard();

        assertEquals(5L, response.getTotalUsers());
        assertEquals(2L, response.getTotalOrders());
        assertEquals(50000.0, response.getTotalRevenue());
        assertEquals(2L, response.getTotalProductsSold());
        assertEquals(1L, response.getPendingOrders());
        assertEquals(1, response.getLowStockProducts().size());
    }
}