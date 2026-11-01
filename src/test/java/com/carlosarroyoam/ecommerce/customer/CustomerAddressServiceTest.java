package com.carlosarroyoam.ecommerce.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse;
import com.carlosarroyoam.ecommerce.customer.entity.Customer;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerAddressServiceTest {
  @Mock private CustomerRepository customerRepository;
  @Mock private CustomerAddressRepository customerAddressRepository;
  @InjectMocks private CustomerAddressService customerAddressService;

  private CustomerAddress address;

  @BeforeEach
  void setUp() {
    Customer customer =
        Customer.builder()
            .id(1L)
            .firstName("José")
            .lastName("García Hernández")
            .email("jose.garcia@example.com")
            .status(CustomerStatus.ACTIVE)
            .build();

    address =
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
            .build();
  }

  @Test
  @DisplayName("Given customer has addresses, when find all by customer id, then returns addresses")
  void givenCustomerHasAddresses_whenFindAllByCustomerId_thenReturnsAddresses() {
    when(customerRepository.existsById(anyLong())).thenReturn(true);
    when(customerAddressRepository.findAllByCustomerId(anyLong())).thenReturn(List.of(address));

    List<CustomerAddressResponse> response = customerAddressService.findAllByCustomerId(1L);

    assertThat(response)
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo(1L);
          assertThat(item.getStreetName()).isEqualTo("C. Leona Vicario");
          assertThat(item.getPostalCode()).isEqualTo("38923");
          assertThat(item.getIsDefault()).isTrue();
          assertThat(item.getCustomerId()).isEqualTo(1L);
        });
  }

  @Test
  @DisplayName(
      "Given customer has no addresses, when find all by customer id, then returns empty list")
  void givenCustomerHasNoAddresses_whenFindAllByCustomerId_thenReturnsEmptyList() {
    when(customerRepository.existsById(anyLong())).thenReturn(true);
    when(customerAddressRepository.findAllByCustomerId(anyLong())).thenReturn(List.of());

    List<CustomerAddressResponse> response = customerAddressService.findAllByCustomerId(1L);

    assertThat(response).isEmpty();
  }

  @Test
  @DisplayName(
      "Given customer does not exist, when find all by customer id, then throws not found exception")
  void givenCustomerDoesNotExist_whenFindAllByCustomerId_thenThrowsNotFoundException() {
    when(customerRepository.existsById(anyLong())).thenReturn(false);

    assertThatThrownBy(() -> customerAddressService.findAllByCustomerId(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.CUSTOMER_NOT_FOUND_EXCEPTION);

    verify(customerAddressRepository, never()).findAllByCustomerId(anyLong());
  }

  @Test
  @DisplayName("Given address belongs to customer, when find by id, then returns address")
  void givenAddressBelongsToCustomer_whenFindById_thenReturnsAddress() {
    when(customerRepository.existsById(anyLong())).thenReturn(true);
    when(customerAddressRepository.findByIdAndCustomerId(anyLong(), anyLong()))
        .thenReturn(Optional.of(address));

    CustomerAddressResponse response = customerAddressService.findById(1L, 1L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getLocality()).isEqualTo("Acámbaro");
    assertThat(response.getCustomerId()).isEqualTo(1L);
    verify(customerAddressRepository).findByIdAndCustomerId(1L, 1L);
  }

  @Test
  @DisplayName(
      "Given customer does not exist, when find by id, then throws customer not found exception")
  void givenCustomerDoesNotExist_whenFindById_thenThrowsCustomerNotFoundException() {
    when(customerRepository.existsById(anyLong())).thenReturn(false);

    assertThatThrownBy(() -> customerAddressService.findById(999L, 1L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.CUSTOMER_NOT_FOUND_EXCEPTION);

    verify(customerAddressRepository, never()).findByIdAndCustomerId(anyLong(), anyLong());
  }

  @Test
  @DisplayName(
      "Given address does not exist for customer, when find by id, then throws address not found exception")
  void givenAddressDoesNotExistForCustomer_whenFindById_thenThrowsAddressNotFoundException() {
    when(customerRepository.existsById(anyLong())).thenReturn(true);
    when(customerAddressRepository.findByIdAndCustomerId(anyLong(), anyLong()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerAddressService.findById(1L, 2L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.CUSTOMER_ADDRESS_NOT_FOUND_EXCEPTION);
  }
}
