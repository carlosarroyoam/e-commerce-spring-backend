package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.customer.CustomerRepository;
import com.carlosarroyoam.ecommerce.customer.entity.Customer;
import java.util.Set;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("customerDetailsService")
public class CustomerDetailsService implements UserDetailsService {
  private final CustomerRepository customerRepository;

  public CustomerDetailsService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return customerRepository
        .findByEmail(email)
        .map(this::mapCustomer)
        .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + email));
  }

  @Transactional(readOnly = true)
  public UserDetails loadUserById(Long customerId) throws UsernameNotFoundException {
    return customerRepository
        .findById(customerId)
        .map(this::mapCustomer)
        .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + customerId));
  }

  private AuthPrincipal mapCustomer(Customer customer) {
    return AuthPrincipal.builder()
        .id(customer.getId())
        .fullName(customer.getFirstName() + " " + customer.getLastName())
        .firstName(customer.getFirstName())
        .lastName(customer.getLastName())
        .email(customer.getEmail())
        .passwordHash(customer.getPasswordHash())
        .status(customer.getStatus().toString())
        .principalType(PrincipalType.CUSTOMER)
        .roles(Set.of("CUSTOMER"))
        .build();
  }
}
