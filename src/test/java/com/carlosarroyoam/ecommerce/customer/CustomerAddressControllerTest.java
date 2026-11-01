package com.carlosarroyoam.ecommerce.customer;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ExceptionLogger;
import com.carlosarroyoam.ecommerce.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.ecommerce.core.exception.ProblemDetailFactory;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import com.carlosarroyoam.ecommerce.support.testutils.TestObjectMappers;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CustomerAddressControllerTest {
  @Mock private CustomerAddressService customerAddressService;
  @InjectMocks private CustomerAddressController customerAddressController;

  private MockMvc mockMvc;
  private CustomerAddressResponse address;

  @BeforeEach
  void setup() {
    address =
        CustomerAddressResponse.builder()
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
            .customerId(1L)
            .build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(customerAddressController)
            .setControllerAdvice(
                new GlobalExceptionHandler(
                    new ProblemDetailFactory(),
                    new ExceptionLogger(),
                    TestMessageSources.messages()))
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase()))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses - Given customer has addresses, when find all, then returns addresses")
  void givenCustomerHasAddresses_whenFindAllCustomerAddresses_thenReturnsAddresses()
      throws Exception {
    when(customerAddressService.findAllByCustomerId(anyLong())).thenReturn(List.of(address));

    mockMvc
        .perform(get("/customers/{customerId}/addresses", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].street_name").value("C. Leona Vicario"))
        .andExpect(jsonPath("$[0].street_number").value("120"))
        .andExpect(jsonPath("$[0].postal_code").value("38923"))
        .andExpect(jsonPath("$[0].phone_number").value("4431232123"))
        .andExpect(jsonPath("$[0].is_default").value(true))
        .andExpect(jsonPath("$[0].customer_id").value(1));
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses - Given customer does not exist, when find all, then returns not found")
  void givenCustomerDoesNotExist_whenFindAllCustomerAddresses_thenReturnsNotFound()
      throws Exception {
    when(customerAddressService.findAllByCustomerId(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.CUSTOMER_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/customers/{customerId}/addresses", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer not found"))
        .andExpect(jsonPath("$.instance").value("/customers/999/addresses"));
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses/{id} - Given address exists, when find by id, then returns address")
  void givenAddressExists_whenFindCustomerAddressById_thenReturnsAddress() throws Exception {
    when(customerAddressService.findById(anyLong(), anyLong())).thenReturn(address);

    mockMvc
        .perform(
            get("/customers/{customerId}/addresses/{addressId}", 1L, 1L)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.locality").value("Acámbaro"))
        .andExpect(jsonPath("$.state").value("Guanajuato"))
        .andExpect(jsonPath("$.country").value("MX"))
        .andExpect(jsonPath("$.customer_id").value(1));
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses/{id} - Given customer does not exist, when find by id, then returns not found")
  void givenCustomerDoesNotExist_whenFindCustomerAddressById_thenReturnsNotFound()
      throws Exception {
    when(customerAddressService.findById(anyLong(), anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.CUSTOMER_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/customers/{customerId}/addresses/{addressId}", 999L, 1L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer not found"))
        .andExpect(jsonPath("$.instance").value("/customers/999/addresses/1"));
  }

  @Test
  @DisplayName(
      "GET /customers/{id}/addresses/{id} - Given address does not exist, when find by id, then returns not found")
  void givenAddressDoesNotExist_whenFindCustomerAddressById_thenReturnsNotFound() throws Exception {
    when(customerAddressService.findById(anyLong(), anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.CUSTOMER_ADDRESS_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/customers/{customerId}/addresses/{addressId}", 1L, 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer address not found"))
        .andExpect(jsonPath("$.instance").value("/customers/1/addresses/999"));
  }
}
