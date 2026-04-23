package com.carlosarroyoam.ecommerce.shipment.dto;

import com.carlosarroyoam.ecommerce.shipment.dto.CarrierResponse.CarrierResponseMapper;
import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Getter
@Setter
@Builder
public class ShipmentResponse {
  private Long id;
  private String trackingNumber;
  private CarrierResponse carrier;
  private Long orderId;
  private LocalDateTime shippedAt;
  private LocalDateTime deliveredAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {CarrierResponseMapper.class})
  public interface ShipmentResponseMapper {
    ShipmentResponseMapper INSTANCE = Mappers.getMapper(ShipmentResponseMapper.class);

    @Mapping(source = "order.id", target = "orderId")
    ShipmentResponse toDto(Shipment entity);

    List<ShipmentResponse> toDtos(List<Shipment> entities);
  }
}
