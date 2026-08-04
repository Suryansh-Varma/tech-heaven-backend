package com.ansh.smart_commerce.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.entity.Product;

class ProductSearchServiceTest {

    private final ProductSearchService service = new ProductSearchService();

    @Test
    void filterByIntent_shouldUseCategoryAndBudget() {
        List<Product> products = List.of(
                TestFixtures.product(1L, "Gaming Laptop Pro", 85000, 4, "Laptops"),
                TestFixtures.product(2L, "Office Laptop", 65000, 7, "Laptops"),
                TestFixtures.product(3L, "Wireless Mouse", 1500, 20, "Accessories"));

        List<Product> filtered = service.filterByIntent(products, "best gaming laptop under 90k");

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(p -> p.getName().contains("Gaming Laptop Pro")));
        assertTrue(filtered.stream().anyMatch(p -> p.getName().contains("Office Laptop")));
    }

    @Test
    void filterByIntent_shouldFallbackToTopProductsWhenNothingMatches() {
        List<Product> products = List.of(
                TestFixtures.product(1L, "Laptop A", 100, 4, "Laptops"),
                TestFixtures.product(2L, "Laptop B", 200, 7, "Laptops"),
                TestFixtures.product(3L, "Laptop C", 300, 20, "Laptops"),
                TestFixtures.product(4L, "Laptop D", 400, 20, "Laptops"),
                TestFixtures.product(5L, "Laptop E", 500, 20, "Laptops"),
                TestFixtures.product(6L, "Laptop F", 600, 20, "Laptops"));

        List<Product> filtered = service.filterByIntent(products, "something unrelated");

        assertEquals(5, filtered.size());
        assertEquals("Laptop A", filtered.get(0).getName());
        assertEquals("Laptop E", filtered.get(4).getName());
    }

    @Test
    void findByName_shouldLocatePartialMatch() {
        List<Product> products = List.of(
                TestFixtures.product(1L, "iPhone 16", 99999, 5, "Phone"),
                TestFixtures.product(2L, "Samsung S25", 89999, 6, "Phone"));

        assertTrue(service.findByName(products, "iphone").isPresent());
        assertEquals("iPhone 16", service.findByName(products, "iphone").orElseThrow().getName());
        assertFalse(service.findByName(products, "unknown").isPresent());
    }

    @Test
    void findAccessories_shouldExcludeBaseProductAndOutOfStockItems() {
        Product base = TestFixtures.product(1L, "Laptop", 70000, 3, "Laptop");
        List<Product> products = List.of(
                base,
                TestFixtures.product(2L, "Mouse", 1000, 10, "Accessory"),
                TestFixtures.product(3L, "Bag", 2500, 0, "Accessory"),
                TestFixtures.product(4L, "Keyboard", 2500, 8, "Accessory"));

        List<Product> accessories = service.findAccessories(products, base);

        assertEquals(2, accessories.size());
        assertTrue(accessories.stream().anyMatch(p -> p.getName().equals("Mouse")));
        assertTrue(accessories.stream().anyMatch(p -> p.getName().equals("Keyboard")));
    }

    @Test
    void extractBudgetAndCategory_shouldParseNaturalLanguage() {
        assertEquals(50000.0, service.extractBudget("show me laptops under 50k").orElseThrow());
        assertEquals("laptop", service.extractCategory("show me a gaming laptop under 90k"));
    }
}