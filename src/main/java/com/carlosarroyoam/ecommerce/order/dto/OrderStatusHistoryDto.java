package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderDto.OrderDtoMapper;
import com.carlosarroyoam.ecommerce.order.dto.OrderStatusDto.OrderStatusDtoMapper;
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
public class OrderStatusHistoryDto {
  private Long id;
  private OrderDto order;
  private OrderStatusDto status;
  private String notes;
  private LocalDateTime changedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      OrderDtoMapper.class, OrderStatusDtoMapper.class })
  public interface OrderStatusHistoryDtoMapper {
    OrderStatusHistoryDtoMapper INSTANCE = Mappers.getMapper(OrderStatusHistoryDtoMapper.class);

    OrderStatusHistoryDto toDto(OrderStatusHistory entity);

    List<OrderStatusHistoryDto> toDtos(List<OrderStatusHistory> entities);
  }
}