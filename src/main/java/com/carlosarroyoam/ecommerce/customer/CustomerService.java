package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse.CustomerResponseMapper;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerSpecs;
import com.carlosarroyoam.ecommerce.customer.entity.Customer;
import com.carlosarroyoam.ecommerce.customer.entity.Customer_;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {
  private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
  private final CustomerRepository customerRepository;

  public CustomerService(final CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

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

  @Transactional(readOnly = true)
  public CustomerResponse findById(Long customerId) {
    Customer customerById = findCustomerByIdOrFail(customerId);
    return CustomerResponseMapper.INSTANCE.toDto(customerById);
  }

  private Customer findCustomerByIdOrFail(Long customerId) {
    return customerRepository
        .findById(customerId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
            });
  }
}
