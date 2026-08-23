package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints REST de consulta de direcciones de un cliente bajo
 * {@code /customers/{customerId}/addresses}.
 */
@RestController
@RequestMapping("/customers/{customerId}/addresses")
public class CustomerAddressController {
  private final CustomerAddressService customerAddressService;

  public CustomerAddressController(final CustomerAddressService customerAddressService) {
    this.customerAddressService = customerAddressService;
  }

  /**
   * Lista todas las direcciones de un cliente.
   *
   * @param customerId el id del cliente
   * @return la lista de direcciones y el estado 200 OK
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<List<CustomerAddressResponse>> findAllByCustomerId(
      @PathVariable Long customerId) {
    List<CustomerAddressResponse> customerAddresses =
        customerAddressService.findAllByCustomerId(customerId);
    return ResponseEntity.ok(customerAddresses);
  }

  /**
   * Obtiene una dirección de un cliente por su id.
   *
   * @param customerId el id del cliente
   * @param addressId el id de la dirección
   * @return la dirección encontrada y el estado 200 OK
   */
  @GetMapping(value = "/{addressId}", produces = "application/json")
  public ResponseEntity<CustomerAddressResponse> findById(
      @PathVariable Long customerId, @PathVariable Long addressId) {
    CustomerAddressResponse customerAddressById =
        customerAddressService.findById(customerId, addressId);
    return ResponseEntity.ok(customerAddressById);
  }
}
