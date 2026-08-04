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
import com.ansh.smart_commerce.dto.CartRequest;
import com.ansh.smart_commerce.dto.CartResponse;
import com.ansh.smart_commerce.entity.CartItem;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.exception.ProductNotFound;
import com.ansh.smart_commerce.exception.UserNotFoundException;
import com.ansh.smart_commerce.repository.CartRepository;
import com.ansh.smart_commerce.repository.ProductRepository;
import com.ansh.smart_commerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCart_shouldCreateNewLineItem() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 1000, 5, "Electronics");
        CartRequest request = buildRequest(user.getId(), product.getId(), 2);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        when(cartRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addToCart(request);

        assertEquals(product.getId(), response.getProductId());
        assertEquals(2, response.getQuantity());
    }

    @Test
    void addToCart_shouldIncreaseExistingQuantity() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 1000, 5, "Electronics");
        CartItem existing = TestFixtures.cartItem(11L, user, product, 1);
        CartRequest request = buildRequest(user.getId(), product.getId(), 2);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(existing));
        when(cartRepository.save(existing)).thenReturn(existing);

        CartResponse response = cartService.addToCart(request);

        assertEquals(3, response.getQuantity());
        verify(cartRepository).save(existing);
    }

    @Test
    void getCart_shouldMapCartResponses() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 1000, 5, "Electronics");
        CartItem item = TestFixtures.cartItem(11L, user, product, 2);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(List.of(item));

        List<CartResponse> responses = cartService.getCart(user.getId());

        assertEquals(1, responses.size());
        assertEquals(2, responses.get(0).getQuantity());
    }

    @Test
    void updateQuantity_shouldDeleteWhenZero() {
        CartItem item = TestFixtures.cartItem(11L, TestFixtures.user(1L, "Alice", "alice@example.com"),
                TestFixtures.product(2L, "Laptop", 1000, 5, "Electronics"), 2);

        when(cartRepository.findById(11L)).thenReturn(Optional.of(item));

        cartService.updateQuantity(11L, 0);

        verify(cartRepository).delete(item);
    }

    @Test
    void removeItem_shouldDeleteExistingCartItem() {
        CartItem item = TestFixtures.cartItem(11L, TestFixtures.user(1L, "Alice", "alice@example.com"),
                TestFixtures.product(2L, "Laptop", 1000, 5, "Electronics"), 2);

        when(cartRepository.findById(11L)).thenReturn(Optional.of(item));

        cartService.removeItem(11L);

        verify(cartRepository).delete(item);
    }

    @Test
    void addToCart_shouldThrowWhenUserMissing() {
        CartRequest request = buildRequest(1L, 2L, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cartService.addToCart(request));
    }

    @Test
    void addToCart_shouldThrowWhenProductMissing() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        CartRequest request = buildRequest(user.getId(), 2L, 1);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFound.class, () -> cartService.addToCart(request));
    }

    private CartRequest buildRequest(Long userId, Long productId, int quantity) {
        CartRequest request = new CartRequest();
        request.setUserId(userId);
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }
}