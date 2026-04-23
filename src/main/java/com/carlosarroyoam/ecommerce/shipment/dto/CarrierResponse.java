package com.carlosarroyoam.ecommerce.shipment.dto;

import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Getter
@Setter
@Builder
public class CarrierResponse {
  private Byte id;
  private String name;
  private LocalDateTime deletedAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface CarrierResponseMapper {
    CarrierResponseMapper INSTANCE = Mappers.getMapper(CarrierResponseMapper.class);

    CarrierResponse toDto(Carrier entity);

    List<CarrierResponse> toDtos(List<Carrier> entities);
  }
}
