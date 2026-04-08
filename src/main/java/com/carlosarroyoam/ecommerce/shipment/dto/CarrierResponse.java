package com.carlosarroyoam.ecommerce.shipment.dto;

import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
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
public class CarrierResponse {
  private Byte id;
  private String name;
  private Boolean isActive;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface CarrierResponseMapper {
    CarrierResponseMapper INSTANCE = Mappers.getMapper(CarrierResponseMapper.class);

    CarrierResponse toDto(Carrier entity);

    List<CarrierResponse> toDtos(List<Carrier> entities);
  }
}