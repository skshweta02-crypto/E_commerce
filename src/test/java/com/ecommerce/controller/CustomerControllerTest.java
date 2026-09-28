package com.ecommerce.controller;

import com.ecommerce.dto.CustomerRequestDTO;
import com.ecommerce.dto.CustomerResponseDTO;
import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCustomer_Success() throws Exception {
        CustomerRequestDTO requestDTO = new CustomerRequestDTO("Alice", "alice@example.com", "1234567890");
        CustomerResponseDTO responseDTO = new CustomerResponseDTO(1L, "Alice", "alice@example.com", "1234567890", LocalDateTime.now());

        when(customerService.createCustomer(any(CustomerRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void createCustomer_ValidationError() throws Exception {
        CustomerRequestDTO invalidDTO = new CustomerRequestDTO("", "invalid-email", "");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void createCustomer_DuplicateEmailConflict() throws Exception {
        CustomerRequestDTO requestDTO = new CustomerRequestDTO("Alice", "alice@example.com", "1234567890");

        when(customerService.createCustomer(any(CustomerRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("Customer with email alice@example.com already exists"));

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void getCustomerById_Success() throws Exception {
        CustomerResponseDTO responseDTO = new CustomerResponseDTO(1L, "Alice", "alice@example.com", "1234567890", LocalDateTime.now());

        when(customerService.getCustomerById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getCustomerById_NotFound() throws Exception {
        when(customerService.getCustomerById(99L)).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void getAllCustomers_Success() throws Exception {
        CustomerResponseDTO responseDTO = new CustomerResponseDTO(1L, "Alice", "alice@example.com", "1234567890", LocalDateTime.now());

        when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(responseDTO));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("alice@example.com"));
    }

    @Test
    void deleteCustomer_Success() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }
}
