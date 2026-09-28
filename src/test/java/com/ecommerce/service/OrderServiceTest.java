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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        customer = new Customer("Jane", "jane@example.com", "12345");
        customer.setId(1L);

        product = new Product("Phone", "Mobile", new BigDecimal("500.00"), "Tech");
        product.setId(2L);

        inventory = new Inventory(product, 10);
        inventory.setId(5L);
    }

    @Test
    void createOrder_Success() {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(2L, 2);
        OrderRequestDTO requestDTO = new OrderRequestDTO(1L, Collections.singletonList(itemDTO));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdWithLock(2L)).thenReturn(Optional.of(inventory));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        OrderResponseDTO response = orderService.createOrder(requestDTO);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(new BigDecimal("1000.00"), response.getTotalAmount());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(8, inventory.getQuantity());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(2L, 20); // requested 20, available 10
        OrderRequestDTO requestDTO = new OrderRequestDTO(1L, Collections.singletonList(itemDTO));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdWithLock(2L)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(requestDTO));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_CustomerNotFound() {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(2L, 1);
        OrderRequestDTO requestDTO = new OrderRequestDTO(99L, Collections.singletonList(itemDTO));

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> orderService.createOrder(requestDTO));
    }

    @Test
    void createOrder_ProductNotFound() {
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(99L, 1);
        OrderRequestDTO requestDTO = new OrderRequestDTO(1L, Collections.singletonList(itemDTO));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> orderService.createOrder(requestDTO));
    }

    @Test
    void createOrder_EmptyItems() {
        OrderRequestDTO requestDTO = new OrderRequestDTO(1L, Collections.emptyList());

        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(requestDTO));
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(99L));
    }

    @Test
    void updateOrderStatus_ValidTransition_Success() {
        Order order = new Order(customer, OrderStatus.PENDING);
        order.setId(100L);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO response = orderService.updateOrderStatus(100L, OrderStatus.CONFIRMED);

        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void updateOrderStatus_InvalidTransition_ThrowsException() {
        Order order = new Order(customer, OrderStatus.SHIPPED);
        order.setId(100L);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderException.class, () -> orderService.updateOrderStatus(100L, OrderStatus.CANCELLED));
    }

    @Test
    void cancelOrder_RestocksInventory_Success() {
        Order order = new Order(customer, OrderStatus.PENDING);
        order.setId(100L);
        OrderItem item = new OrderItem(product, 3, product.getPrice());
        order.addItem(item);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdWithLock(product.getId())).thenReturn(Optional.of(inventory));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO response = orderService.cancelOrder(100L);

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertEquals(13, inventory.getQuantity()); // original 10 + restocked 3 = 13
        verify(inventoryRepository, times(1)).save(inventory);
    }
}
