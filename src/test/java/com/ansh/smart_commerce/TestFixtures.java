package com.ansh.smart_commerce;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.test.util.ReflectionTestUtils;

import com.ansh.smart_commerce.entity.Address;
import com.ansh.smart_commerce.entity.CartItem;
import com.ansh.smart_commerce.entity.Coupon;
import com.ansh.smart_commerce.entity.Order;
import com.ansh.smart_commerce.entity.OrderItem;
import com.ansh.smart_commerce.entity.Payment;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.Review;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.entity.Wishlist;
import com.ansh.smart_commerce.enums.DiscountType;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.enums.PaymentMethod;
import com.ansh.smart_commerce.enums.PaymentStatus;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static User user(long id, String name, String email) {
        User user = new User(name, email, "secret");
        user.setId(id);
        return user;
    }

    public static Product product(long id, String name, double cost, int stock, String category) {
        Product product = new Product(name, cost, stock, category, name.toLowerCase() + ".png");
        product.setId(id);
        return product;
    }

    public static Order order(long id, User user, OrderStatus status) {
        Order order = new Order(user, LocalDateTime.now(), 0.0, status, null, 0.0, 0.0, 0.0);
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    public static OrderItem orderItem(Order order, Product product, int quantity, double price) {
        return new OrderItem(order, product, quantity, price);
    }

    public static Payment payment(long id, Order order, PaymentMethod method, PaymentStatus status,
                                  String transactionId, double amount) {
        Payment payment = new Payment(order, method, status, transactionId, amount, LocalDateTime.now());
        ReflectionTestUtils.setField(payment, "id", id);
        return payment;
    }

    public static Address address(long id, User user, boolean isDefault) {
        Address address = new Address();
        ReflectionTestUtils.setField(address, "id", id);
        address.setUser(user);
        address.setFullName(user.getName());
        address.setPhoneNumber("9876543210");
        address.setHouseNumber("12A");
        address.setStreet("Main Street");
        address.setLandmark("Near Market");
        address.setCity("Bengaluru");
        address.setState("Karnataka");
        address.setPostalCode("560001");
        address.setCountry("India");
        address.setDefault(isDefault);
        return address;
    }

    public static CartItem cartItem(long id, User user, Product product, int quantity) {
        CartItem cartItem = new CartItem(user, product, quantity);
        ReflectionTestUtils.setField(cartItem, "id", id);
        return cartItem;
    }

    public static Coupon coupon(long id, String code, DiscountType type, double discountValue,
                                double minimumAmount, LocalDate expiryDate, boolean active) {
        Coupon coupon = new Coupon();
        ReflectionTestUtils.setField(coupon, "id", id);
        coupon.setCode(code);
        coupon.setDiscountType(type);
        coupon.setDiscountValue(discountValue);
        coupon.setMinimumAmount(minimumAmount);
        coupon.setExpiryDate(expiryDate);
        coupon.setActive(active);
        return coupon;
    }

    public static Review review(long id, User user, Product product, int rating, String comment) {
        Review review = new Review(user, product, rating, comment, LocalDateTime.now());
        ReflectionTestUtils.setField(review, "id", id);
        return review;
    }

    public static Wishlist wishlist(long id, User user, Product product) {
        Wishlist wishlist = new Wishlist(user, product);
        ReflectionTestUtils.setField(wishlist, "id", id);
        return wishlist;
    }
}