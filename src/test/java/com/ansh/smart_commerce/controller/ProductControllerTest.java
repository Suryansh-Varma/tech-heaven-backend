package com.ansh.smart_commerce.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addProduct_shouldRejectNonRootUsers() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice@example.com", null, List.of(new SimpleGrantedAuthority("USER"))));

        mockMvc.perform(post("/products").contentType("application/json").content("{\"name\":\"Laptop\",\"cost\":50000,\"stock\":10}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addProduct_shouldCreateForRootUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("root@techheaven.com", null, List.of(new SimpleGrantedAuthority("USER"))));
        Product product = TestFixtures.product(1L, "Laptop", 50000, 10, "Laptops");
        when(productService.addProduct(org.mockito.ArgumentMatchers.any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/products").contentType("application/json").content("{\"name\":\"Laptop\",\"cost\":50000,\"stock\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Laptop"));
    }

    @Test
    void getAllProducts_shouldReturnProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(TestFixtures.product(1L, "Laptop", 50000, 10, "Laptops")));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Laptop"));
    }

    @Test
    void deleteProduct_shouldCallServiceForRootUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("root", null, List.of(new SimpleGrantedAuthority("USER"))));

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(productService).deleteProduct(1L);
    }

    @Test
    void getLowStockProducts_shouldRejectNonRootUsers() throws Exception {
        mockMvc.perform(get("/products/low-stock"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}