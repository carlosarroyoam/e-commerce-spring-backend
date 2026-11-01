package com.carlosarroyoam.ecommerce.customer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PaginationResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerSpecs;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerStatus;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import com.carlosarroyoam.ecommerce.support.testutils.TestObjectMappers;
import java.time.LocalDate;
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
class CustomerControllerTest {
  @Mock private CustomerService customerService;
  @InjectMocks private CustomerController customerController;

  private MockMvc mockMvc;
  private CustomerResponse customer;

  @BeforeEach
  void setup() {
    customer =
        CustomerResponse.builder()
            .id(1L)
            .firstName("José")
            .lastName("García Hernández")
            .phoneNumber("6181234567")
            .email("jose.garcia@example.com")
            .status(CustomerStatus.ACTIVE)
            .addresses(
                List.of(
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
                        .build()))
            .build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(customerController)
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
      "GET /customers - Given customers exist, when find all, then returns paged customers")
  void givenCustomersExist_whenFindAllCustomers_thenReturnsPagedCustomers() throws Exception {
    PagedResponse<CustomerResponse> page =
        PagedResponse.<CustomerResponse>builder()
            .items(List.of(customer))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(1).totalPages(1).build())
            .build();

    when(customerService.findAll(any(CustomerSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/customers")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].first_name").value("José"))
        .andExpect(jsonPath("$.items[0].phone_number").value("6181234567"))
        .andExpect(jsonPath("$.items[0].addresses[0].customer_id").value(1))
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(10))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName("GET /customers - Given filters, when find all, then passes filters to service")
  void givenFilters_whenFindAllCustomers_thenPassesFiltersToService() throws Exception {
    PagedResponse<CustomerResponse> page =
        PagedResponse.<CustomerResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(0).totalPages(0).build())
            .build();

    when(customerService.findAll(any(CustomerSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/customers")
                .queryParam("firstName", "José")
                .queryParam("lastName", "García")
                .queryParam("email", "jose.garcia@example.com")
                .queryParam("status", "SUSPENDED")
                .queryParam("startDate", "2025-01-01")
                .queryParam("endDate", "2025-01-31"))
        .andExpect(status().isOk());

    verify(customerService)
        .findAll(
            argThat((CustomerSpecs specs) ->
                "José".equals(specs.getFirstName())
                    && "García".equals(specs.getLastName())
                    && "jose.garcia@example.com".equals(specs.getEmail())
                    && specs.getStatus() == CustomerStatus.SUSPENDED
                    && LocalDate.of(2025, 1, 1).equals(specs.getStartDate())
                    && LocalDate.of(2025, 1, 31).equals(specs.getEndDate())),
            any());
  }

  @Test
  @DisplayName(
      "GET /customers - Given invalid email filter, when find all, then returns unprocessable entity")
  void givenInvalidEmailFilter_whenFindAllCustomers_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(get("/customers").queryParam("email", "no-es-un-email"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.instance").value("/customers"))
        .andExpect(jsonPath("$.errors.email").value("Email should be an valid email address"));

    verifyNoInteractions(customerService);
  }

  @Test
  @DisplayName(
      "GET /customers/{id} - Given customer exists, when find by id, then returns customer")
  void givenCustomerExists_whenFindCustomerById_thenReturnsCustomer() throws Exception {
    when(customerService.findById(anyLong())).thenReturn(customer);

    mockMvc
        .perform(get("/customers/{customerId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.last_name").value("García Hernández"))
        .andExpect(jsonPath("$.email").value("jose.garcia@example.com"))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.addresses[0].postal_code").value("38923"))
        .andExpect(jsonPath("$.addresses[0].is_default").value(true));
  }

  @Test
  @DisplayName(
      "GET /customers/{id} - Given customer does not exist, when find by id, then returns not found")
  void givenCustomerDoesNotExist_whenFindCustomerById_thenReturnsNotFound() throws Exception {
    when(customerService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.CUSTOMER_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/customers/{customerId}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer not found"))
        .andExpect(jsonPath("$.instance").value("/customers/999"));
  }
}
