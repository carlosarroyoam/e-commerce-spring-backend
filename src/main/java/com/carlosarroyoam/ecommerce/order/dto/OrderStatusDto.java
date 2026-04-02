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
public class OrderStatusDto {
  private Byte id;
  private String title;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface OrderStatusDtoMapper {
    OrderStatusDtoMapper INSTANCE = Mappers.getMapper(OrderStatusDtoMapper.class);

    OrderStatusDto toDto(OrderStatus entity);

    List<OrderStatusDto> toDtos(List<OrderStatus> entities);
  }
}