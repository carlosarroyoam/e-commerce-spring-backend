package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.order.entity.OrderPaymentStatus;
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
public class OrderPaymentStatusDto {
  private Byte id;
  private String name;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface OrderPaymentStatusDtoMapper {
    OrderPaymentStatusDtoMapper INSTANCE = Mappers.getMapper(OrderPaymentStatusDtoMapper.class);

    OrderPaymentStatusDto toDto(OrderPaymentStatus entity);

    List<OrderPaymentStatusDto> toDtos(List<OrderPaymentStatus> entities);
  }
}