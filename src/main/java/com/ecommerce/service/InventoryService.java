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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public InventoryResponseDTO setStock(InventoryRequestDTO requestDTO) {
        if (requestDTO.getQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(requestDTO.getProductId()));

        Inventory inventory = inventoryRepository.findByProductId(requestDTO.getProductId())
                .orElseGet(() -> new Inventory(product, 0));

        inventory.setQuantity(requestDTO.getQuantity());
        Inventory saved = inventoryRepository.save(inventory);
        log.info("Set stock for product ID: {} to quantity: {}", product.getId(), saved.getQuantity());
        return InventoryResponseDTO.fromEntity(saved);
    }

    @Transactional
    public InventoryResponseDTO addStock(Long productId, Integer quantityToAdd) {
        if (quantityToAdd == null || quantityToAdd <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }

        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        inventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        Inventory updated = inventoryRepository.save(inventory);
        log.info("Added {} stock to product ID: {}. New quantity: {}", quantityToAdd, productId, updated.getQuantity());
        return InventoryResponseDTO.fromEntity(updated);
    }

    @Transactional
    public InventoryResponseDTO reduceStock(Long productId, Integer quantityToReduce) {
        if (quantityToReduce == null || quantityToReduce <= 0) {
            throw new IllegalArgumentException("Quantity to reduce must be positive");
        }

        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        if (inventory.getQuantity() < quantityToReduce) {
            log.warn("Cannot reduce stock for product ID: {}. Available: {}, Requested reduction: {}",
                    productId, inventory.getQuantity(), quantityToReduce);
            throw new InsufficientStockException(productId, inventory.getQuantity(), quantityToReduce);
        }

        inventory.setQuantity(inventory.getQuantity() - quantityToReduce);
        Inventory updated = inventoryRepository.save(inventory);
        log.info("Reduced {} stock from product ID: {}. Remaining quantity: {}", quantityToReduce, productId, updated.getQuantity());
        return InventoryResponseDTO.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public InventoryResponseDTO getStockByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));
        return InventoryResponseDTO.fromEntity(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> getAllInventories() {
        return inventoryRepository.findAll().stream()
                .map(InventoryResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
