package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderStatusHistoryResponse.OrderStatusHistoryResponseMapper;
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
  private List<ShipmentResponse> shipments;
  private List<OrderStatusHistoryResponse> statusHistory;
  private LocalDateTime createdAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      ShipmentResponseMapper.class, OrderStatusHistoryResponseMapper.class })
  public interface OrderTrackResponseMapper {
    OrderTrackResponseMapper INSTANCE = Mappers.getMapper(OrderTrackResponseMapper.class);

    OrderTrackResponse toDto(Order entity);
  }
}
