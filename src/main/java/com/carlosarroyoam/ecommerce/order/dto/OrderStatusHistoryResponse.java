package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.order.entity.OrderStatusHistory;
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
public class OrderStatusHistoryResponse {
  private Long id;
  private String notes;
  private LocalDateTime changedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface OrderStatusHistoryResponseMapper {
    OrderStatusHistoryResponseMapper INSTANCE = Mappers
        .getMapper(OrderStatusHistoryResponseMapper.class);

    OrderStatusHistoryResponse toDto(OrderStatusHistory entity);

    List<OrderStatusHistoryResponse> toDtos(List<OrderStatusHistory> entities);
  }
}