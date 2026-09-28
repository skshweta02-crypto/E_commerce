package com.ecommerce.service;

import com.ecommerce.dto.InventoryRequestDTO;
import com.ecommerce.dto.InventoryResponseDTO;
import com.ecommerce.entity.Inventory;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InventoryNotFoundException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        product = new Product("Laptop", "Tech", new BigDecimal("1000.00"), "Electronics");
        product.setId(1L);
        inventory = new Inventory(product, 50);
        inventory.setId(10L);
    }

    @Test
    void testSetStock_Success() {
        InventoryRequestDTO requestDTO = new InventoryRequestDTO(1L, 100);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryResponseDTO response = inventoryService.setStock(requestDTO);

        assertNotNull(response);
        assertEquals(100, inventory.getQuantity());
    }

    @Test
    void testSetStock_ProductNotFound() {
        InventoryRequestDTO requestDTO = new InventoryRequestDTO(99L, 100);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> inventoryService.setStock(requestDTO));
    }

    @Test
    void testAddStock_Success() {
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryResponseDTO response = inventoryService.addStock(1L, 20);

        assertNotNull(response);
        assertEquals(70, inventory.getQuantity());
    }

    @Test
    void testReduceStock_Success() {
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryResponseDTO response = inventoryService.reduceStock(1L, 30);

        assertNotNull(response);
        assertEquals(20, inventory.getQuantity());
    }

    @Test
    void testReduceStock_InsufficientStock() {
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class, () -> inventoryService.reduceStock(1L, 100));
    }

    @Test
    void testReduceStock_NegativePrevention() {
        assertThrows(IllegalArgumentException.class, () -> inventoryService.reduceStock(1L, -5));
    }

    @Test
    void testGetStockByProductId_NotFound() {
        when(inventoryRepository.findByProductId(99L)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class, () -> inventoryService.getStockByProductId(99L));
    }
}
