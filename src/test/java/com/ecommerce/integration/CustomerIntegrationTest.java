package com.ecommerce.integration;

import com.ecommerce.dto.CustomerRequestDTO;
import com.ecommerce.dto.CustomerResponseDTO;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Test
    void testCustomerLifecycleAndDuplicateEmailPrevention() {
        CustomerRequestDTO req1 = new CustomerRequestDTO("Alice Johnson", "alice.j@example.com", "1112223333");
        CustomerResponseDTO created = customerService.createCustomer(req1);
        assertNotNull(created.getId());

        CustomerRequestDTO reqDuplicate = new CustomerRequestDTO("Alice Duplicate", "alice.j@example.com", "9999999999");
        assertThrows(DuplicateResourceException.class, () -> customerService.createCustomer(reqDuplicate));

        CustomerRequestDTO reqUpdate = new CustomerRequestDTO("Alice Smith", "alice.smith@example.com", "1112223333");
        CustomerResponseDTO updated = customerService.updateCustomer(created.getId(), reqUpdate);
        assertEquals("alice.smith@example.com", updated.getEmail());
    }
}
