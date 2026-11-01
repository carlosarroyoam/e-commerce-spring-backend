package com.carlosarroyoam.ecommerce.shipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.order.entity.Order;
import com.carlosarroyoam.ecommerce.shipment.dto.CarrierResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentSpecs;
import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {
  @Mock private ShipmentRepository shipmentRepository;
  @Mock private CarrierRepository carrierRepository;
  @InjectMocks private ShipmentService shipmentService;

  private Carrier carrier;
  private Shipment shipment;

  @BeforeEach
  void setUp() {
    carrier = Carrier.builder().id((byte) 1).name("FedEx").build();

    shipment =
        Shipment.builder()
            .id(1L)
            .trackingNumber("FX123456789012")
            .carrier(carrier)
            .order(Order.builder().id(1L).build())
            .shippedAt(LocalDateTime.of(2025, 1, 16, 9, 0))
            .deliveredAt(LocalDateTime.of(2025, 1, 18, 14, 20))
            .build();
  }

  @Test
  @DisplayName("Given shipments exist, when find all, then returns paged shipments")
  void givenShipmentsExist_whenFindAll_thenReturnsPagedShipments() {
    ShipmentSpecs shipmentSpecs = ShipmentSpecs.builder().orderId(1L).build();
    Pageable pageable = PageRequest.of(0, 10);
    when(shipmentRepository.findAll(
            ArgumentMatchers.<Specification<Shipment>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(shipment), pageable, 1));

    PagedResponse<ShipmentResponse> response = shipmentService.findAll(shipmentSpecs, pageable);

    assertThat(response.getItems())
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo(1L);
          assertThat(item.getTrackingNumber()).isEqualTo("FX123456789012");
          assertThat(item.getCarrier().getName()).isEqualTo("FedEx");
          assertThat(item.getOrderId()).isEqualTo(1L);
        });
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given shipment exists, when find by id, then returns shipment")
  void givenShipmentExists_whenFindById_thenReturnsShipment() {
    when(shipmentRepository.findById(anyLong())).thenReturn(Optional.of(shipment));

    ShipmentResponse response = shipmentService.findById(1L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getTrackingNumber()).isEqualTo("FX123456789012");
    assertThat(response.getCarrier().getId()).isEqualTo((byte) 1);
    assertThat(response.getOrderId()).isEqualTo(1L);
    assertThat(response.getDeliveredAt()).isEqualTo(LocalDateTime.of(2025, 1, 18, 14, 20));
  }

  @Test
  @DisplayName("Given shipment does not exist, when find by id, then throws not found exception")
  void givenShipmentDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(shipmentRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> shipmentService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.SHIPMENT_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given shipments exist for order, when find all by order id, then returns shipments")
  void givenShipmentsExistForOrder_whenFindAllByOrderId_thenReturnsShipments() {
    when(shipmentRepository.findAllByOrderId(anyLong())).thenReturn(List.of(shipment));

    List<ShipmentResponse> response = shipmentService.findAllByOrderId(1L);

    assertThat(response).hasSize(1);
    assertThat(response.get(0).getId()).isEqualTo(1L);
    assertThat(response.get(0).getOrderId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Given no shipment for order, when find all by order id, then returns empty list")
  void givenNoShipmentForOrder_whenFindAllByOrderId_thenReturnsEmptyList() {
    when(shipmentRepository.findAllByOrderId(anyLong())).thenReturn(List.of());

    List<ShipmentResponse> response = shipmentService.findAllByOrderId(3L);

    assertThat(response).isEmpty();
  }

  @Test
  @DisplayName("Given carriers exist, when find all active carriers, then returns carriers")
  void givenCarriersExist_whenFindAllActiveCarriers_thenReturnsCarriers() {
    when(carrierRepository.findAllByDeletedAtIsNull())
        .thenReturn(List.of(carrier, Carrier.builder().id((byte) 2).name("UPS").build()));

    List<CarrierResponse> response = shipmentService.findAllActiveCarriers();

    assertThat(response).hasSize(2);
    assertThat(response).extracting("id").containsExactly((byte) 1, (byte) 2);
    assertThat(response).extracting("name").containsExactly("FedEx", "UPS");
    assertThat(response).extracting("deletedAt").containsOnlyNulls();
  }
}
