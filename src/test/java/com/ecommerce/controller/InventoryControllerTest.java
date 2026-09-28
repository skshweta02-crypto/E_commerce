package com.ecommerce.controller;

import com.ecommerce.dto.InventoryRequestDTO;
import com.ecommerce.dto.InventoryResponseDTO;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InventoryNotFoundException;
import com.ecommerce.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void setStock_Success() throws Exception {
        InventoryRequestDTO requestDTO = new InventoryRequestDTO(1L, 100);
        InventoryResponseDTO responseDTO = new InventoryResponseDTO(10L, 1L, "Laptop", 100, LocalDateTime.now());

        when(inventoryService.setStock(any(InventoryRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    void getStockByProductId_Success() throws Exception {
        InventoryResponseDTO responseDTO = new InventoryResponseDTO(10L, 1L, "Laptop", 50, LocalDateTime.now());

        when(inventoryService.getStockByProductId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/inventory/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(50));
    }

    @Test
    void getStockByProductId_NotFound() throws Exception {
        when(inventoryService.getStockByProductId(99L)).thenThrow(new InventoryNotFoundException(99L));

        mockMvc.perform(get("/api/inventory/product/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void addStock_Success() throws Exception {
        InventoryResponseDTO responseDTO = new InventoryResponseDTO(10L, 1L, "Laptop", 70, LocalDateTime.now());

        when(inventoryService.addStock(1L, 20)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/inventory/product/1/add")
                .param("quantity", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(70));
    }

    @Test
    void reduceStock_InsufficientStock() throws Exception {
        when(inventoryService.reduceStock(1L, 100)).thenThrow(new InsufficientStockException(1L, 50, 100));

        mockMvc.perform(put("/api/inventory/product/1/reduce")
                .param("quantity", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
    }
}
