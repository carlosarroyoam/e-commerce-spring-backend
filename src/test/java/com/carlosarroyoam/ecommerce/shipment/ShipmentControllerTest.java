package com.carlosarroyoam.ecommerce.shipment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
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
import com.carlosarroyoam.ecommerce.shipment.dto.CarrierResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentSpecs;
import com.carlosarroyoam.ecommerce.support.testutils.TestMessageSources;
import com.carlosarroyoam.ecommerce.support.testutils.TestObjectMappers;
import java.time.LocalDateTime;
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
class ShipmentControllerTest {
  @Mock private ShipmentService shipmentService;
  @InjectMocks private ShipmentController shipmentController;

  private MockMvc mockMvc;
  private ShipmentResponse shipment;

  @BeforeEach
  void setup() {
    shipment =
        ShipmentResponse.builder()
            .id(1L)
            .trackingNumber("FX123456789012")
            .carrier(CarrierResponse.builder().id((byte) 1).name("FedEx").build())
            .orderId(1L)
            .shippedAt(LocalDateTime.of(2025, 1, 16, 9, 0))
            .build();

    mockMvc =
        MockMvcBuilders.standaloneSetup(shipmentController)
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
      "GET /shipments - Given shipments exist, when find all, then returns paged shipments")
  void givenShipmentsExist_whenFindAllShipments_thenReturnsPagedShipments() throws Exception {
    PagedResponse<ShipmentResponse> page =
        PagedResponse.<ShipmentResponse>builder()
            .items(List.of(shipment))
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(1).totalPages(1).build())
            .build();

    when(shipmentService.findAll(any(ShipmentSpecs.class), any())).thenReturn(page);

    mockMvc
        .perform(
            get("/shipments")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].tracking_number").value("FX123456789012"))
        .andExpect(jsonPath("$.items[0].carrier.name").value("FedEx"))
        .andExpect(jsonPath("$.items[0].order_id").value(1))
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(10))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName(
      "GET /shipments - Given order id filter, when find all, then passes filter to service")
  void givenOrderIdFilter_whenFindAllShipments_thenPassesFilterToService() throws Exception {
    PagedResponse<ShipmentResponse> page =
        PagedResponse.<ShipmentResponse>builder()
            .items(List.of())
            .pagination(
                PaginationResponse.builder().page(0).size(10).totalItems(0).totalPages(0).build())
            .build();

    when(shipmentService.findAll(any(ShipmentSpecs.class), any())).thenReturn(page);

    mockMvc.perform(get("/shipments").queryParam("orderId", "2")).andExpect(status().isOk());

    verify(shipmentService)
        .findAll(
            argThat((ShipmentSpecs specs) -> Long.valueOf(2L).equals(specs.getOrderId())), any());
  }

  @Test
  @DisplayName(
      "GET /shipments/{id} - Given shipment exists, when find by id, then returns shipment")
  void givenShipmentExists_whenFindShipmentById_thenReturnsShipment() throws Exception {
    when(shipmentService.findById(anyLong())).thenReturn(shipment);

    mockMvc
        .perform(get("/shipments/{shipmentId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.tracking_number").value("FX123456789012"))
        .andExpect(jsonPath("$.carrier.id").value(1))
        .andExpect(jsonPath("$.order_id").value(1))
        .andExpect(jsonPath("$.delivered_at").doesNotExist());
  }

  @Test
  @DisplayName(
      "GET /shipments/{id} - Given shipment does not exist, when find by id, then returns not found")
  void givenShipmentDoesNotExist_whenFindShipmentById_thenReturnsNotFound() throws Exception {
    when(shipmentService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.SHIPMENT_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/shipments/{shipmentId}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Shipment not found"))
        .andExpect(jsonPath("$.instance").value("/shipments/999"));
  }

  @Test
  @DisplayName(
      "GET /shipments/order/{orderId} - Given shipments exist for order, when find shipments by order id, then returns shipments")
  void givenShipmentsExistForOrder_whenFindShipmentsByOrderId_thenReturnsShipments()
      throws Exception {
    when(shipmentService.findAllByOrderId(anyLong())).thenReturn(List.of(shipment));

    mockMvc
        .perform(get("/shipments/order/{orderId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].order_id").value(1));

    verify(shipmentService).findAllByOrderId(1L);
  }

  @Test
  @DisplayName(
      "GET /shipments/order/{orderId} - Given no shipment for order, when find shipments by order id, then returns empty list")
  void givenNoShipmentForOrder_whenFindShipmentsByOrderId_thenReturnsEmptyList() throws Exception {
    when(shipmentService.findAllByOrderId(anyLong())).thenReturn(List.of());

    mockMvc
        .perform(get("/shipments/order/{orderId}", 3L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName(
      "GET /shipments/carriers - Given carriers exist, when find carriers, then returns carriers")
  void givenCarriersExist_whenFindAllCarriers_thenReturnsCarriers() throws Exception {
    when(shipmentService.findAllActiveCarriers())
        .thenReturn(
            List.of(
                CarrierResponse.builder().id((byte) 1).name("FedEx").build(),
                CarrierResponse.builder().id((byte) 2).name("UPS").build()));

    mockMvc
        .perform(get("/shipments/carriers").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("FedEx"))
        .andExpect(jsonPath("$[1].name").value("UPS"));
  }
}
