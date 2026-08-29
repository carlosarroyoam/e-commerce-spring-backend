package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerSpecs;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone los endpoints REST de consulta de clientes bajo {@code /customers}. */
@RestController
@RequestMapping("/customers")
public class CustomerController {
  private final CustomerService customerService;

  public CustomerController(final CustomerService customerService) {
    this.customerService = customerService;
  }

  /**
   * Lista los clientes de forma paginada y filtrable.
   *
   * @param customerSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de clientes y el estado 200 OK
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<CustomerResponse>> findAll(
      @Valid @ModelAttribute CustomerSpecs customerSpecs,
      @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
    PagedResponse<CustomerResponse> customers = customerService.findAll(customerSpecs, pageable);
    return ResponseEntity.ok(customers);
  }

  /**
   * Obtiene un cliente por su id.
   *
   * @param customerId el id del cliente
   * @return el cliente encontrado y el estado 200 OK
   */
  @GetMapping(value = "/{customerId}", produces = "application/json")
  public ResponseEntity<CustomerResponse> findById(@PathVariable Long customerId) {
    CustomerResponse customerById = customerService.findById(customerId);
    return ResponseEntity.ok(customerById);
  }
}
