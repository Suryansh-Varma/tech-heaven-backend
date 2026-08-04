package com.ansh.smart_commerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.entity.Order;
import com.ansh.smart_commerce.entity.OrderItem;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.exception.ProductNotFound;
import com.ansh.smart_commerce.repository.OrderRepository;
import com.ansh.smart_commerce.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void addProduct_shouldPersistProduct() {
        Product product = TestFixtures.product(0L, "Laptop", 50000, 10, "Laptops");
        when(productRepository.save(product)).thenReturn(product);

        assertEquals("Laptop", productService.addProduct(product).getName());
    }

    @Test
    void getAllProducts_shouldReturnRepositoryResults() {
        when(productRepository.findAll()).thenReturn(List.of(TestFixtures.product(1L, "Laptop", 50000, 10, "Laptops")));

        assertEquals(1, productService.getAllProducts().size());
    }

    @Test
    void getLowStockProducts_shouldReturnLowStockOrUnavailableItems() {
        Product lowStock = TestFixtures.product(1L, "Mouse", 1000, 3, "Accessories");
        Product unavailable = TestFixtures.product(2L, "Headphones", 2000, 8, "Accessories");
        unavailable.setAvailable(false);
        Product healthy = TestFixtures.product(3L, "Laptop", 50000, 10, "Laptops");

        when(productRepository.findAll()).thenReturn(List.of(lowStock, unavailable, healthy));

        List<Product> products = productService.getLowStockProducts();

        assertEquals(2, products.size());
    }

    @Test
    void updateProduct_shouldCancelPendingOrdersWhenProductBecomesUnavailable() {
        Product existing = TestFixtures.product(1L, "Laptop", 50000, 10, "Laptops");
        Product updated = TestFixtures.product(0L, "Laptop Pro", 55000, 0, "Laptops");
        Order order = TestFixtures.order(10L, TestFixtures.user(2L, "Bob", "bob@example.com"), OrderStatus.PENDING);
        order.setOrderItems(List.of(new OrderItem(order, existing, 1, 50000)));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderService.cancelOrder(10L)).thenReturn(null);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(1L, updated);

        assertEquals(0, result.getStock());
        verify(orderService).cancelOrder(10L);
    }

    @Test
    void deleteProduct_shouldRemoveExistingProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void getProductById_shouldThrowWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFound.class, () -> productService.getProductById(99L));
    }
}