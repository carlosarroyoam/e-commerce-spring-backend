package com.carlosarroyoam.ecommerce.shipment;

import static com.carlosarroyoam.ecommerce.support.security.AuthRequestPostProcessors.withoutAuthentication;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
import com.carlosarroyoam.ecommerce.support.AbstractIntegrationTest;
import com.carlosarroyoam.ecommerce.support.security.JwtTestTokenFactory;
import com.carlosarroyoam.ecommerce.support.testutils.JsonUtils;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShipmentControllerIT extends AbstractIntegrationTest {
  private static final long ADMIN_ID = 1L;
  private static final String ADMIN_EMAIL = "carlos.arroyo@e-commerce.com";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private JwtTestTokenFactory jwtTestTokenFactory;
  @Autowired private CarrierRepository carrierRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    String adminToken = jwtTestTokenFactory.staffToken(ADMIN_ID, ADMIN_EMAIL, List.of("ADMIN"));

    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .defaultRequest(get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
            .build();
  }

  @Test
  @DisplayName(
      "GET /shipments - Given shipments exist, when find all, then returns paged shipments")
  void givenShipmentsExist_whenFindAllShipments_thenReturnsPagedShipments() throws Exception {
    String expectedJson = JsonUtils.readJson("shipments/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/shipments"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /shipments - Given order id filter, when find all, then returns shipments of order")
  void givenOrderIdFilter_whenFindAllShipments_thenReturnsShipmentsOfOrder() throws Exception {
    mockMvc
        .perform(get("/shipments").queryParam("orderId", "2"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(2))
        .andExpect(jsonPath("$.items[0].order_id").value(2))
        .andExpect(jsonPath("$.pagination.total_items").value(1));
  }

  @Test
  @DisplayName("GET /shipments - Given no authentication, when find all, then returns unauthorized")
  void givenNoAuthentication_whenFindAllShipments_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/shipments").with(withoutAuthentication()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/shipments"));
  }

  @Test
  @DisplayName(
      "GET /shipments/{id} - Given shipment exists, when find by id, then returns shipment")
  void givenShipmentExists_whenFindShipmentById_thenReturnsShipment() throws Exception {
    String expectedJson = JsonUtils.readJson("shipments/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/shipments/{shipmentId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /shipments/{id} - Given shipment does not exist, when find by id, then returns not found")
  void givenShipmentDoesNotExist_whenFindShipmentById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/shipments/{shipmentId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Shipment not found"))
        .andExpect(jsonPath("$.instance").value("/shipments/9999"));
  }

  @Test
  @DisplayName(
      "GET /shipments/order/{orderId} - Given shipments exist for order, when find shipments by order id, then returns shipments")
  void givenShipmentsExistForOrder_whenFindShipmentsByOrderId_thenReturnsShipments()
      throws Exception {
    String expectedJson = JsonUtils.readJson("shipments/find-by-order.json");

    String responseJson =
        mockMvc
            .perform(get("/shipments/order/{orderId}", 2L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /shipments/order/{orderId} - Given order without shipment, when find shipments by order id, then returns empty list")
  void givenOrderWithoutShipment_whenFindShipmentsByOrderId_thenReturnsEmptyList()
      throws Exception {
    mockMvc
        .perform(get("/shipments/order/{orderId}", 3L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName(
      "GET /shipments/carriers - Given carriers exist, when find all carriers, then returns carriers")
  void givenCarriersExist_whenFindAllCarriers_thenReturnsCarriers() throws Exception {
    String expectedJson = JsonUtils.readJson("shipments/carriers.json");

    String responseJson =
        mockMvc
            .perform(get("/shipments/carriers"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.length()").value(5))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /shipments/carriers - Given a deleted carrier, when find all carriers, then excludes it")
  void givenDeletedCarrier_whenFindAllCarriers_thenExcludesIt() throws Exception {
    carrierRepository.saveAndFlush(
        Carrier.builder().name("Discontinued Express").deletedAt(LocalDateTime.now()).build());

    mockMvc
        .perform(get("/shipments/carriers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(5))
        .andExpect(jsonPath("$[*].name", not(hasItem("Discontinued Express"))));
  }
}
