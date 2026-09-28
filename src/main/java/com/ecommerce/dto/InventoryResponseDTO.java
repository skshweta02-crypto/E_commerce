package com.ecommerce.dto;

import com.ecommerce.entity.Inventory;
import java.time.LocalDateTime;

public class InventoryResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private LocalDateTime updatedAt;

    public InventoryResponseDTO() {
    }

    public InventoryResponseDTO(Long id, Long productId, String productName, Integer quantity, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }

    public static InventoryResponseDTO fromEntity(Inventory inventory) {
        return new InventoryResponseDTO(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                inventory.getQuantity(),
                inventory.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
