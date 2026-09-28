package com.ecommerce.service;

import com.ecommerce.dto.CustomerRequestDTO;
import com.ecommerce.dto.CustomerResponseDTO;
import com.ecommerce.entity.Customer;
import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer("John Doe", "john@example.com", "1234567890");
        sampleCustomer.setId(1L);
    }

    @Test
    void testCreateCustomer_Success() {
        CustomerRequestDTO requestDTO = new CustomerRequestDTO("John Doe", "john@example.com", "1234567890");
        when(customerRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        CustomerResponseDTO responseDTO = customerService.createCustomer(requestDTO);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("john@example.com", responseDTO.getEmail());
    }

    @Test
    void testCreateCustomer_DuplicateEmail() {
        CustomerRequestDTO requestDTO = new CustomerRequestDTO("John Doe", "john@example.com", "1234567890");
        when(customerRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.createCustomer(requestDTO));
    }

    @Test
    void testGetCustomerById_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));

        CustomerResponseDTO responseDTO = customerService.getCustomerById(1L);

        assertNotNull(responseDTO);
        assertEquals("John Doe", responseDTO.getName());
    }

    @Test
    void testGetCustomerById_NotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(99L));
    }

    @Test
    void testGetAllCustomers() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(sampleCustomer));

        List<CustomerResponseDTO> customers = customerService.getAllCustomers();

        assertEquals(1, customers.size());
        assertEquals("John Doe", customers.get(0).getName());
    }

    @Test
    void testUpdateCustomer_Success() {
        CustomerRequestDTO updateDTO = new CustomerRequestDTO("John Smith", "john@example.com", "0987654321");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.existsByEmailAndIdNot("john@example.com", 1L)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        CustomerResponseDTO responseDTO = customerService.updateCustomer(1L, updateDTO);

        assertNotNull(responseDTO);
        verify(customerRepository, times(1)).save(sampleCustomer);
    }

    @Test
    void testDeleteCustomer_Success() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        assertDoesNotThrow(() -> customerService.deleteCustomer(1L));
        verify(customerRepository, times(1)).deleteById(1L);
    }
}
