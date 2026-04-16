package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
  List<CustomerAddress> findAllByCustomerId(Long customerId);

  Optional<CustomerAddress> findByIdAndCustomerId(Long addressId, Long customerId);
}
