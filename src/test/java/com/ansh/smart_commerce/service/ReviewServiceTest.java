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
import com.ansh.smart_commerce.dto.ReviewRequest;
import com.ansh.smart_commerce.dto.ReviewResponse;
import com.ansh.smart_commerce.entity.Order;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.Review;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.exception.ProductNotFound;
import com.ansh.smart_commerce.exception.ReviewNotAllowedException;
import com.ansh.smart_commerce.repository.OrderRepository;
import com.ansh.smart_commerce.repository.ProductRepository;
import com.ansh.smart_commerce.repository.ReviewRepository;
import com.ansh.smart_commerce.security.SecurityHelper;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void addReview_shouldPersistAfterPurchaseCheck() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 50000, 5, "Laptops");
        Order order = TestFixtures.order(10L, user, OrderStatus.CONFIRMED);
        order.setOrderItems(List.of(TestFixtures.orderItem(order, product, 1, 50000)));

        ReviewRequest request = buildRequest(user.getId(), product.getId(), 5, "Great product");

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.findByUser(user)).thenReturn(List.of(order));
        when(reviewRepository.existsByUserAndProduct(user, product)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.addReview(request);

        assertEquals(product.getId(), response.getProductId());
        assertEquals(5, response.getRating());
    }

    @Test
    void addReview_shouldRejectWhenProductWasNotPurchased() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 50000, 5, "Laptops");
        ReviewRequest request = buildRequest(user.getId(), product.getId(), 5, "Great product");

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.findByUser(user)).thenReturn(List.of());

        assertThrows(ReviewNotAllowedException.class, () -> reviewService.addReview(request));
    }

    @Test
    void addReview_shouldRejectDuplicateReview() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Product product = TestFixtures.product(2L, "Laptop", 50000, 5, "Laptops");
        Order order = TestFixtures.order(10L, user, OrderStatus.CONFIRMED);
        order.setOrderItems(List.of(TestFixtures.orderItem(order, product, 1, 50000)));
        ReviewRequest request = buildRequest(user.getId(), product.getId(), 5, "Great product");

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.findByUser(user)).thenReturn(List.of(order));
        when(reviewRepository.existsByUserAndProduct(user, product)).thenReturn(true);

        assertThrows(ReviewNotAllowedException.class, () -> reviewService.addReview(request));
    }

    @Test
    void getProductReviews_shouldMapResponses() {
        Product product = TestFixtures.product(2L, "Laptop", 50000, 5, "Laptops");
        Review review = TestFixtures.review(3L, TestFixtures.user(1L, "Alice", "alice@example.com"), product, 4, "Nice");

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(reviewRepository.findByProduct(product)).thenReturn(List.of(review));

        List<ReviewResponse> responses = reviewService.getProductReviews(product.getId());

        assertEquals(1, responses.size());
        assertEquals(4, responses.get(0).getRating());
    }

    @Test
    void getAverageRating_shouldReturnRepositoryAverage() {
        Product product = TestFixtures.product(2L, "Laptop", 50000, 5, "Laptops");
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(reviewRepository.findAverageRatingByProduct(product)).thenReturn(4.5);

        assertEquals(4.5, reviewService.getAverageRating(product.getId()));
    }

    @Test
    void updateReview_shouldRejectForeignReviewer() {
        User owner = TestFixtures.user(1L, "Alice", "alice@example.com");
        User other = TestFixtures.user(2L, "Bob", "bob@example.com");
        Review review = TestFixtures.review(3L, owner, TestFixtures.product(2L, "Laptop", 50000, 5, "Laptops"), 4, "Nice");

        when(securityHelper.getCurrentUser()).thenReturn(other);
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));

        assertThrows(ReviewNotAllowedException.class, () -> reviewService.updateReview(3L, buildRequest(other.getId(), 2L, 5, "Edit")));
    }

    @Test
    void deleteReview_shouldDeleteOwnedReview() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Review review = TestFixtures.review(3L, user, TestFixtures.product(2L, "Laptop", 50000, 5, "Laptops"), 4, "Nice");

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(3L);

        verify(reviewRepository).delete(review);
    }

    private ReviewRequest buildRequest(Long userId, Long productId, int rating, String comment) {
        ReviewRequest request = new ReviewRequest();
        request.setUserId(userId);
        request.setProductId(productId);
        request.setRating(rating);
        request.setComment(comment);
        return request;
    }
}