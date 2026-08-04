package com.ansh.smart_commerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.dto.CouponRequest;
import com.ansh.smart_commerce.entity.Coupon;
import com.ansh.smart_commerce.enums.DiscountType;
import com.ansh.smart_commerce.exception.CouponExpiredException;
import com.ansh.smart_commerce.repository.CouponRepository;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void createCoupon_shouldUppercaseCodeAndPersist() {
        CouponRequest request = buildRequest("save10", DiscountType.PERCENTAGE, 10, 500, LocalDate.now().plusDays(5));

        when(couponRepository.save(org.mockito.ArgumentMatchers.any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Coupon coupon = couponService.createCoupon(request);

        assertEquals("SAVE10", coupon.getCode());
        assertEquals(10, coupon.getDiscountValue());
        verify(couponRepository).save(coupon);
    }

    @Test
    void applyDiscount_shouldCalculatePercentageDiscount() {
        Coupon coupon = TestFixtures.coupon(1L, "SAVE10", DiscountType.PERCENTAGE, 10, 500, LocalDate.now().plusDays(5), true);
        when(couponRepository.findByCodeAndActiveTrue("SAVE10")).thenReturn(Optional.of(coupon));

        double finalAmount = couponService.applyDiscount("save10", 1000);

        assertEquals(900.0, finalAmount);
    }

    @Test
    void applyDiscount_shouldRejectBelowMinimumAmount() {
        Coupon coupon = TestFixtures.coupon(1L, "SAVE10", DiscountType.FLAT, 100, 2000, LocalDate.now().plusDays(5), true);
        when(couponRepository.findByCodeAndActiveTrue("SAVE10")).thenReturn(Optional.of(coupon));

        assertThrows(IllegalArgumentException.class, () -> couponService.applyDiscount("SAVE10", 1000));
    }

    @Test
    void applyDiscount_shouldDeactivateExpiredCoupon() {
        Coupon coupon = TestFixtures.coupon(1L, "SAVE10", DiscountType.FLAT, 100, 0, LocalDate.now().minusDays(1), true);
        when(couponRepository.findByCodeAndActiveTrue("SAVE10")).thenReturn(Optional.of(coupon));
        when(couponRepository.save(coupon)).thenReturn(coupon);

        assertThrows(CouponExpiredException.class, () -> couponService.applyDiscount("SAVE10", 1000));
        verify(couponRepository).save(coupon);
    }

    @Test
    void deactivateCoupon_shouldMarkInactive() {
        Coupon coupon = TestFixtures.coupon(1L, "SAVE10", DiscountType.FLAT, 100, 0, LocalDate.now().plusDays(1), true);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(coupon)).thenReturn(coupon);

        couponService.deactivateCoupon(1L);

        assertEquals(false, coupon.isActive());
        verify(couponRepository).save(coupon);
    }

    @Test
    void getAllCoupons_shouldReturnAllCoupons() {
        when(couponRepository.findAll()).thenReturn(List.of(TestFixtures.coupon(1L, "SAVE10", DiscountType.FLAT, 100, 0, LocalDate.now().plusDays(1), true)));

        assertEquals(1, couponService.getAllCoupons().size());
    }

    private CouponRequest buildRequest(String code, DiscountType type, double value, double minimum, LocalDate expiry) {
        CouponRequest request = new CouponRequest();
        request.setCode(code);
        request.setDiscountType(type);
        request.setDiscountValue(value);
        request.setMinimumAmount(minimum);
        request.setExpiryDate(expiry);
        return request;
    }
}