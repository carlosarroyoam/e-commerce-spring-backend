package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse.CustomerResponseMapper;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerSpecs;
import com.carlosarroyoam.ecommerce.customer.entity.Customer;
import com.carlosarroyoam.ecommerce.customer.entity.Customer_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lógica de negocio para consultar clientes ({@link Customer}). */
@Service
public class CustomerService {
  private final CustomerRepository customerRepository;

  public CustomerService(final CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  /**
   * Obtiene una página de clientes que cumplen los filtros indicados.
   *
   * @param customerSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de {@link CustomerResponse} resultante
   */
  @Transactional(readOnly = true)
  public PagedResponse<CustomerResponse> findAll(CustomerSpecs customerSpecs, Pageable pageable) {
    Specification<Customer> spec =
        SpecificationBuilder.<Customer>builder()
            .likeIfPresent(root -> root.get(Customer_.firstName), customerSpecs.getFirstName())
            .likeIfPresent(root -> root.get(Customer_.lastName), customerSpecs.getLastName())
            .likeIfPresent(root -> root.get(Customer_.email), customerSpecs.getEmail())
            .equalsIfPresent(root -> root.get(Customer_.status), customerSpecs.getStatus())
            .betweenDatesIfPresent(
                root -> root.get(Customer_.createdAt),
                customerSpecs.getStartDate(),
                customerSpecs.getEndDate())
            .build();

    Page<Customer> customers = customerRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        customers.map(CustomerResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un cliente por su id.
   *
   * @param customerId el id del cliente
   * @return el {@link CustomerResponse} correspondiente
   * @throws ResourceNotFoundException con 404 si no existe un cliente con ese id
   */
  @Transactional(readOnly = true)
  public CustomerResponse findById(Long customerId) {
    Customer customerById = findCustomerByIdOrFail(customerId);
    return CustomerResponseMapper.INSTANCE.toDto(customerById);
  }

  /**
   * Busca un cliente por su id o lanza una excepción si no existe.
   *
   * @param customerId el id del cliente
   * @return el {@link Customer} encontrado
   * @throws ResourceNotFoundException con 404 si no existe un cliente con ese id
   */
  private Customer findCustomerByIdOrFail(Long customerId) {
    return customerRepository
        .findById(customerId)
        .orElseThrow(() -> new ResourceNotFoundException(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION));
  }
}
