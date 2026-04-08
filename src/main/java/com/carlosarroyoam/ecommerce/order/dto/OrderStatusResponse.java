package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.order.entity.OrderStatus;
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
public class OrderStatusResponse {
  private Byte id;
  private String title;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface OrderStatusResponseMapper {
    OrderStatusResponseMapper INSTANCE = Mappers.getMapper(OrderStatusResponseMapper.class);

    OrderStatusResponse toDto(OrderStatus entity);

    List<OrderStatusResponse> toDtos(List<OrderStatus> entities);
  }
}