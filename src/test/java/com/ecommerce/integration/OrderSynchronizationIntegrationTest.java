package com.ecommerce.integration;

import com.ecommerce.dto.OrderItemRequestDTO;
import com.ecommerce.dto.OrderRequestDTO;
import com.ecommerce.dto.OrderResponseDTO;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Inventory;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderSynchronizationIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Customer testCustomer;
    private Product testProduct;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(new Customer("Integration User", "integration@test.com", "999888777"));
        testProduct = productRepository.save(new Product("Gaming Laptop", "High end laptop", new BigDecimal("1500.00"), "Electronics"));
        testInventory = inventoryRepository.save(new Inventory(testProduct, 5));
    }

    @Test
    void testSuccessfulOrder_DeductsInventory() {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(testProduct.getId(), 2);
        OrderRequestDTO requestDTO = new OrderRequestDTO(testCustomer.getId(), Collections.singletonList(itemDTO));

        OrderResponseDTO response = orderService.createOrder(requestDTO);

        assertNotNull(response);
        assertEquals(new BigDecimal("3000.00"), response.getTotalAmount());

        Inventory updatedInventory = inventoryRepository.findByProductId(testProduct.getId()).orElseThrow(AssertionError::new);
        assertEquals(3, updatedInventory.getQuantity()); // 5 - 2 = 3
    }

    @Test
    void testInsufficientStock_RollsBackTransactionAndDoesNotCreateOrder() {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(testProduct.getId(), 10); // requests 10, only 5 available
        OrderRequestDTO requestDTO = new OrderRequestDTO(testCustomer.getId(), Collections.singletonList(itemDTO));

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(requestDTO));

        Inventory unchangeInventory = inventoryRepository.findByProductId(testProduct.getId()).orElseThrow(AssertionError::new);
        assertEquals(5, unchangeInventory.getQuantity()); // Stock remains unchanged
        assertEquals(0, orderRepository.count()); // No partial order left in database
    }
}
