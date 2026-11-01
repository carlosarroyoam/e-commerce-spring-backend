package com.carlosarroyoam.ecommerce.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerSpecs;
import com.carlosarroyoam.ecommerce.customer.entity.Customer;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
  @Mock private CustomerRepository customerRepository;
  @Spy private Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);
  @InjectMocks private CustomerService customerService;

  private Customer customer;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now(clock);

    customer =
        Customer.builder()
            .id(1L)
            .firstName("José")
            .lastName("García Hernández")
            .phoneNumber("6181234567")
            .email("jose.garcia@example.com")
            .passwordHash("hash")
            .status(CustomerStatus.ACTIVE)
            .createdAt(now.minusDays(1))
            .updatedAt(now.minusDays(1))
            .build();
    customer.setAddresses(
        List.of(
            CustomerAddress.builder()
                .id(1L)
                .streetName("C. Leona Vicario")
                .streetNumber("120")
                .sublocality("Centro")
                .locality("Acámbaro")
                .state("Guanajuato")
                .postalCode("38923")
                .country("MX")
                .phoneNumber("4431232123")
                .isDefault(true)
                .customer(customer)
                .build()));
  }

  @Test
  @DisplayName("Given customers exist, when find all, then returns paged customers")
  void givenCustomersExist_whenFindAll_thenReturnsPagedCustomers() {
    CustomerSpecs customerSpecs =
        CustomerSpecs.builder().firstName("José").status(CustomerStatus.ACTIVE).build();
    Pageable pageable = PageRequest.of(0, 10);
    when(customerRepository.findAll(
            ArgumentMatchers.<Specification<Customer>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(customer), pageable, 1));

    PagedResponse<CustomerResponse> response = customerService.findAll(customerSpecs, pageable);

    assertThat(response.getItems())
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo(1L);
          assertThat(item.getEmail()).isEqualTo("jose.garcia@example.com");
          assertThat(item.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
          assertThat(item.getAddresses()).extracting("customerId").containsExactly(1L);
        });
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given no customers match, when find all, then returns empty page")
  void givenNoCustomersMatch_whenFindAll_thenReturnsEmptyPage() {
    CustomerSpecs customerSpecs = CustomerSpecs.builder().status(CustomerStatus.DELETED).build();
    Pageable pageable = PageRequest.of(0, 10);
    when(customerRepository.findAll(
            ArgumentMatchers.<Specification<Customer>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    PagedResponse<CustomerResponse> response = customerService.findAll(customerSpecs, pageable);

    assertThat(response.getItems()).isEmpty();
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isZero();
    assertThat(response.getPagination().getTotalPages()).isZero();
  }

  @Test
  @DisplayName("Given customer exists, when find by id, then returns customer")
  void givenCustomerExists_whenFindById_thenReturnsCustomer() {
    when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

    CustomerResponse response = customerService.findById(1L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getFirstName()).isEqualTo("José");
    assertThat(response.getEmail()).isEqualTo("jose.garcia@example.com");
    assertThat(response.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    assertThat(response.getAddresses()).hasSize(1);
  }

  @Test
  @DisplayName("Given customer does not exist, when find by id, then throws not found exception")
  void givenCustomerDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.CUSTOMER_NOT_FOUND_EXCEPTION);
  }
}
