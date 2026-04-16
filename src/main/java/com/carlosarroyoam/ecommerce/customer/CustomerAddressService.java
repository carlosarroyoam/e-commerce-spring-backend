package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse.CustomerAddressResponseMapper;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerAddressService {
  private static final Logger log = LoggerFactory.getLogger(CustomerAddressService.class);
  private final CustomerRepository customerRepository;
  private final CustomerAddressRepository customerAddressRepository;

  public CustomerAddressService(final CustomerRepository customerRepository,
      final CustomerAddressRepository customerAddressRepository) {
    this.customerRepository = customerRepository;
    this.customerAddressRepository = customerAddressRepository;
  }

  @Transactional(readOnly = true)
  public List<CustomerAddressResponse> findAllByCustomerId(Long customerId) {
    validateCustomerExists(customerId);

    List<CustomerAddress> addresses = customerAddressRepository.findAllByCustomerId(customerId);

    return CustomerAddressResponseMapper.INSTANCE.toDtos(addresses);
  }

  @Transactional(readOnly = true)
  public CustomerAddressResponse findById(Long customerId, Long addressId) {
    validateCustomerExists(customerId);

    CustomerAddress customerAddress = customerAddressRepository
        .findByIdAndCustomerId(addressId, customerId)
        .orElseThrow(() -> {
          log.warn(AppMessages.CUSTOMER_ADDRESS_NOT_FOUND_EXCEPTION);
          return new ResponseStatusException(HttpStatus.NOT_FOUND,
              AppMessages.CUSTOMER_ADDRESS_NOT_FOUND_EXCEPTION);
        });

    return CustomerAddressResponseMapper.INSTANCE.toDto(customerAddress);
  }

  private void validateCustomerExists(Long customerId) {
    if (!customerRepository.existsById(customerId)) {
      log.warn(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
    }
  }
}
