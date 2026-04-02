package com.carlosarroyoam.ecommerce.shipment.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderDto;
import com.carlosarroyoam.ecommerce.order.dto.OrderDto.OrderDtoMapper;
import com.carlosarroyoam.ecommerce.shipment.dto.CarrierDto.CarrierDtoMapper;
import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
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
public class ShipmentDto {
  private Long id;
  private OrderDto order;
  private CarrierDto carrier;
  private String trackingNumber;
  private LocalDateTime shippedAt;
  private LocalDateTime deliveredAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      OrderDtoMapper.class, CarrierDtoMapper.class })
  public interface ShipmentDtoMapper {
    ShipmentDtoMapper INSTANCE = Mappers.getMapper(ShipmentDtoMapper.class);

    ShipmentDto toDto(Shipment entity);

    List<ShipmentDto> toDtos(List<Shipment> entities);
  }
}