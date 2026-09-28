package com.ecommerce.exception;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
