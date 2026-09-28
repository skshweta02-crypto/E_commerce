package com.ecommerce.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testResourceNotFoundException() {
        ProductNotFoundException ex = new ProductNotFoundException(1L);
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getError());
        assertEquals("Product not found with id: 1", response.getBody().getMessage());
    }

    @Test
    void testDuplicateResourceException() {
        DuplicateResourceException ex = new DuplicateResourceException("Email exists");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateResourceException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("DUPLICATE_RESOURCE", response.getBody().getError());
    }

    @Test
    void testInsufficientStockException() {
        InsufficientStockException ex = new InsufficientStockException(1L, 2, 5);
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientStockException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("INSUFFICIENT_STOCK", response.getBody().getError());
    }

    @Test
    void testGlobalException() {
        RuntimeException ex = new RuntimeException("Unexpected database failure");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals("An unexpected internal error occurred", response.getBody().getMessage());
    }
}
