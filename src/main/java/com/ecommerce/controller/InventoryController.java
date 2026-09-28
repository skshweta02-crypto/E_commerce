package com.ecommerce.controller;

import com.ecommerce.dto.InventoryRequestDTO;
import com.ecommerce.dto.InventoryResponseDTO;
import com.ecommerce.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory Management", description = "Endpoints for checking, updating, adding, and reducing stock levels")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @Operation(summary = "Set stock level", description = "Initialize or overwrite stock quantity for a product")
    public ResponseEntity<InventoryResponseDTO> setStock(@Valid @RequestBody InventoryRequestDTO requestDTO) {
        InventoryResponseDTO responseDTO = inventoryService.setStock(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Get all inventory levels", description = "Retrieve stock status for all products")
    public ResponseEntity<List<InventoryResponseDTO>> getAllInventories() {
        List<InventoryResponseDTO> inventories = inventoryService.getAllInventories();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stock by product ID", description = "Check available stock for a specific product")
    public ResponseEntity<InventoryResponseDTO> getStockByProductId(@PathVariable Long productId) {
        InventoryResponseDTO responseDTO = inventoryService.getStockByProductId(productId);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/product/{productId}/add")
    @Operation(summary = "Add stock", description = "Increase stock quantity for a product")
    public ResponseEntity<InventoryResponseDTO> addStock(@PathVariable Long productId,
                                                          @RequestParam Integer quantity) {
        InventoryResponseDTO responseDTO = inventoryService.addStock(productId, quantity);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/product/{productId}/reduce")
    @Operation(summary = "Reduce stock", description = "Decrease stock quantity for a product with negative stock prevention")
    public ResponseEntity<InventoryResponseDTO> reduceStock(@PathVariable Long productId,
                                                             @RequestParam Integer quantity) {
        InventoryResponseDTO responseDTO = inventoryService.reduceStock(productId, quantity);
        return ResponseEntity.ok(responseDTO);
    }
}
