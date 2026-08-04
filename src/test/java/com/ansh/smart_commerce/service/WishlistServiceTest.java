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
import com.ansh.smart_commerce.dto.WishlistResponse;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.entity.Wishlist;
import com.ansh.smart_commerce.exception.ProductNotFound;
import com.ansh.smart_commerce.exception.UserNotFoundException;
import com.ansh.smart_commerce.repository.ProductRepository;
import com.ansh.smart_commerce.repository.UserRepository;
import com.ansh.smart_commerce.repository.WishlistRepository;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    void addToWishlist_shouldCreateEntry() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 1000, 5, "Electronics");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserAndProduct(user, product)).thenReturn(false);
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WishlistResponse response = wishlistService.addToWishlist(1L, 2L);

        assertEquals(2L, response.getProductId());
    }

    @Test
    void addToWishlist_shouldRejectDuplicates() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 1000, 5, "Electronics");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserAndProduct(user, product)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> wishlistService.addToWishlist(1L, 2L));
    }

    @Test
    void removeFromWishlist_shouldDeleteExistingEntry() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 1000, 5, "Electronics");
        Wishlist wishlist = TestFixtures.wishlist(3L, user, product);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(wishlist));

        wishlistService.removeFromWishlist(1L, 2L);

        verify(wishlistRepository).delete(wishlist);
    }

    @Test
    void getWishlist_shouldMapResponses() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Wishlist wishlist = TestFixtures.wishlist(3L, user, TestFixtures.product(2L, "Laptop", 1000, 5, "Electronics"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wishlistRepository.findByUser(user)).thenReturn(List.of(wishlist));

        List<WishlistResponse> responses = wishlistService.getWishlist(1L);

        assertEquals(1, responses.size());
        assertEquals(2L, responses.get(0).getProductId());
    }

    @Test
    void addToWishlist_shouldThrowWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> wishlistService.addToWishlist(1L, 2L));
    }

    @Test
    void addToWishlist_shouldThrowWhenProductMissing() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFound.class, () -> wishlistService.addToWishlist(1L, 2L));
    }
}