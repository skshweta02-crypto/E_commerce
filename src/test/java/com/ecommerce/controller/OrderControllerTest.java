package com.ecommerce.controller;

import com.ecommerce.dto.OrderItemRequestDTO;
import com.ecommerce.dto.OrderRequestDTO;
import com.ecommerce.dto.OrderResponseDTO;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.service.OrderService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_Success() throws Exception {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(1L, 2);
        OrderRequestDTO requestDTO = new OrderRequestDTO(1L, Collections.singletonList(itemDTO));
        OrderResponseDTO responseDTO = new OrderResponseDTO(100L, 1L, "Jane", new BigDecimal("1000.00"), OrderStatus.PENDING, Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now());

        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrderById_Success() throws Exception {
        OrderResponseDTO responseDTO = new OrderResponseDTO(100L, 1L, "Jane", new BigDecimal("1000.00"), OrderStatus.PENDING, Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now());

        when(orderService.getOrderById(100L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/orders/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(1000.00));
    }

    @Test
    void getOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(99L)).thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void createOrder_CustomerNotFound() throws Exception {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(1L, 2);
        OrderRequestDTO requestDTO = new OrderRequestDTO(99L, Collections.singletonList(itemDTO));

        when(orderService.createOrder(any(OrderRequestDTO.class))).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void updateOrderStatus_Success() throws Exception {
        OrderResponseDTO responseDTO = new OrderResponseDTO(100L, 1L, "Jane", new BigDecimal("1000.00"), OrderStatus.CONFIRMED, Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now());

        when(orderService.updateOrderStatus(100L, OrderStatus.CONFIRMED)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/orders/100/status")
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void cancelOrder_Success() throws Exception {
        OrderResponseDTO responseDTO = new OrderResponseDTO(100L, 1L, "Jane", new BigDecimal("1000.00"), OrderStatus.CANCELLED, Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now());

        when(orderService.cancelOrder(100L)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/orders/100/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
