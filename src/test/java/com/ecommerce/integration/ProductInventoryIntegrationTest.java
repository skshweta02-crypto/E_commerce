package com.ecommerce.integration;

import com.ecommerce.dto.InventoryRequestDTO;
import com.ecommerce.dto.InventoryResponseDTO;
import com.ecommerce.dto.ProductRequestDTO;
import com.ecommerce.dto.ProductResponseDTO;
import com.ecommerce.service.InventoryService;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductInventoryIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void testCreateProductAndInitializeInventory() {
        ProductRequestDTO productReq = new ProductRequestDTO("Monitor 4K", "32 inch 4K display", new BigDecimal("450.00"), "Displays");
        ProductResponseDTO createdProduct = productService.createProduct(productReq);
        assertNotNull(createdProduct.getId());

        InventoryRequestDTO inventoryReq = new InventoryRequestDTO(createdProduct.getId(), 25);
        InventoryResponseDTO inventoryResp = inventoryService.setStock(inventoryReq);

        assertEquals(25, inventoryResp.getQuantity());
        assertEquals("Monitor 4K", inventoryResp.getProductName());

        InventoryResponseDTO added = inventoryService.addStock(createdProduct.getId(), 10);
        assertEquals(35, added.getQuantity());

        InventoryResponseDTO reduced = inventoryService.reduceStock(createdProduct.getId(), 5);
        assertEquals(30, reduced.getQuantity());
    }
}
