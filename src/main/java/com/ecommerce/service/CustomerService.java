package com.ecommerce.service;

import com.ecommerce.dto.CustomerRequestDTO;
import com.ecommerce.dto.CustomerResponseDTO;
import com.ecommerce.entity.Customer;
import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {
        if (customerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Customer with email " + requestDTO.getEmail() + " already exists");
        }

        Customer customer = new Customer(
                requestDTO.getName(),
                requestDTO.getEmail(),
                requestDTO.getPhone()
        );
        Customer savedCustomer = customerRepository.save(customer);
        return CustomerResponseDTO.fromEntity(savedCustomer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return CustomerResponseDTO.fromEntity(customer);
    }

    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (customerRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            throw new DuplicateResourceException("Customer with email " + requestDTO.getEmail() + " already exists");
        }

        customer.setName(requestDTO.getName());
        customer.setEmail(requestDTO.getEmail());
        customer.setPhone(requestDTO.getPhone());

        Customer updatedCustomer = customerRepository.save(customer);
        return CustomerResponseDTO.fromEntity(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        customerRepository.deleteById(id);
    }
}
