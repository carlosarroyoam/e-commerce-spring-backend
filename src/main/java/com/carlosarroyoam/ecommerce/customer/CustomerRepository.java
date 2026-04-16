package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerRepository
    extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
}
