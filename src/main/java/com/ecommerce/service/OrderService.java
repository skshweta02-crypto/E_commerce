package com.ecommerce.service;

import com.ecommerce.dto.OrderItemRequestDTO;
import com.ecommerce.dto.OrderRequestDTO;
import com.ecommerce.dto.OrderResponseDTO;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Inventory;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        if (requestDTO.getItems() == null || requestDTO.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }

        Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(requestDTO.getCustomerId()));

        Order order = new Order(customer, OrderStatus.PENDING);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDTO : requestDTO.getItems()) {
            if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                throw new InvalidOrderException("Quantity for product ID " + itemDTO.getProductId() + " must be greater than zero");
            }

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemDTO.getProductId()));

            Inventory inventory = inventoryRepository.findByProductIdWithLock(product.getId())
                    .orElseThrow(() -> new InsufficientStockException("Inventory not initialized for product: " + product.getName()));

            if (inventory.getQuantity() < itemDTO.getQuantity()) {
                log.warn("Insufficient stock for product ID: {}. Available: {}, Requested: {}",
                        product.getId(), inventory.getQuantity(), itemDTO.getQuantity());
                throw new InsufficientStockException(product.getId(), inventory.getQuantity(), itemDTO.getQuantity());
            }

            inventory.setQuantity(inventory.getQuantity() - itemDTO.getQuantity());
            inventoryRepository.save(inventory);

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemSubtotal);

            OrderItem orderItem = new OrderItem(product, itemDTO.getQuantity(), product.getPrice());
            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        log.info("Successfully created order ID: {} for customer ID: {} with total amount: {}",
                savedOrder.getId(), customer.getId(), totalAmount);
        return OrderResponseDTO.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderResponseDTO.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (newStatus == OrderStatus.CANCELLED) {
            return cancelOrder(orderId);
        }

        if (!order.getStatus().canTransitionTo(newStatus)) {
            log.warn("Invalid order status transition attempt for order ID: {} from {} to {}",
                    orderId, order.getStatus(), newStatus);
            throw new InvalidOrderException(String.format("Cannot transition order status from %s to %s",
                    order.getStatus(), newStatus));
        }

        log.info("Updating order ID: {} status from {} to {}", orderId, order.getStatus(), newStatus);
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return OrderResponseDTO.fromEntity(updatedOrder);
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Order is already cancelled");
        }

        if (!order.getStatus().canTransitionTo(OrderStatus.CANCELLED)) {
            log.warn("Cannot cancel order ID: {} in status: {}", orderId, order.getStatus());
            throw new InvalidOrderException("Cannot cancel order in status: " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository.findByProductIdWithLock(item.getProduct().getId())
                    .orElseGet(() -> new Inventory(item.getProduct(), 0));

            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);
            log.info("Restocked quantity: {} for product ID: {} due to order cancellation ID: {}",
                    item.getQuantity(), item.getProduct().getId(), orderId);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        log.info("Successfully cancelled order ID: {}", orderId);
        return OrderResponseDTO.fromEntity(updatedOrder);
    }
}
