package com.ecommerce.controller;

import com.ecommerce.dto.ProductRequestDTO;
import com.ecommerce.dto.ProductResponseDTO;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProduct_Success() throws Exception {
        ProductRequestDTO requestDTO = new ProductRequestDTO("Phone", "Smartphone", new BigDecimal("800.00"), "Electronics");
        ProductResponseDTO responseDTO = new ProductResponseDTO(1L, "Phone", "Smartphone", new BigDecimal("800.00"), "Electronics", LocalDateTime.now(), LocalDateTime.now());

        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @Test
    void createProduct_ValidationError() throws Exception {
        ProductRequestDTO invalidDTO = new ProductRequestDTO("", "", new BigDecimal("-10.00"), "Electronics");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void getProductById_Success() throws Exception {
        ProductResponseDTO responseDTO = new ProductResponseDTO(1L, "Phone", "Smartphone", new BigDecimal("800.00"), "Electronics", LocalDateTime.now(), LocalDateTime.now());

        when(productService.getProductById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @Test
    void getProductById_NotFound() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void getAllProducts_Success() throws Exception {
        ProductResponseDTO responseDTO = new ProductResponseDTO(1L, "Phone", "Smartphone", new BigDecimal("800.00"), "Electronics", LocalDateTime.now(), LocalDateTime.now());

        when(productService.getAllProducts()).thenReturn(Collections.singletonList(responseDTO));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Phone"));
    }

    @Test
    void deleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}
