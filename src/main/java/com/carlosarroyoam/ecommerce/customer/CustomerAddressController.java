package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers/{customerId}/addresses")
public class CustomerAddressController {
  private final CustomerAddressService customerAddressService;

  public CustomerAddressController(final CustomerAddressService customerAddressService) {
    this.customerAddressService = customerAddressService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<List<CustomerAddressResponse>> findAllByCustomerId(
      @PathVariable Long customerId) {
    List<CustomerAddressResponse> addresses = customerAddressService
        .findAllByCustomerId(customerId);
    return ResponseEntity.ok(addresses);
  }

  @GetMapping(value = "/{addressId}", produces = "application/json")
  public ResponseEntity<CustomerAddressResponse> findById(@PathVariable Long customerId,
      @PathVariable Long addressId) {
    CustomerAddressResponse address = customerAddressService.findById(customerId, addressId);
    return ResponseEntity.ok(address);
  }
}
