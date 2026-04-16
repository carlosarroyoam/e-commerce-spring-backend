package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.order.entity.Order;
import com.carlosarroyoam.ecommerce.order.entity.OrderStatus;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentResponse.ShipmentResponseMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderTrackResponse {
  private String orderNumber;
  private OrderStatus status;
  private BigDecimal total;
  private ShipmentResponse shipment;
  private List<OrderStatusHistoryResponse> statusHistory;
  private LocalDateTime createdAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      ShipmentResponseMapper.class })
  public interface OrderTrackResponseMapper {
    OrderTrackResponseMapper INSTANCE = Mappers.getMapper(OrderTrackResponseMapper.class);

    @Mapping(target = "shipment", expression = "java(mapShipment(entity))")
    OrderTrackResponse toDto(Order entity);

    default ShipmentResponse mapShipment(Order entity) {
      if (entity.getShipments() == null || entity.getShipments().isEmpty()) {
        return null;
      }
      return ShipmentResponseMapper.INSTANCE.toDto(entity.getShipments().get(0));
    }
  }
}
