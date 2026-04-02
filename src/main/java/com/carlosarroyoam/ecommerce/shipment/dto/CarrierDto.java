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
public class CarrierDto {
  private Byte id;
  private String name;
  private Boolean active;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface CarrierDtoMapper {
    CarrierDtoMapper INSTANCE = Mappers.getMapper(CarrierDtoMapper.class);

    CarrierDto toDto(Carrier entity);

    List<CarrierDto> toDtos(List<Carrier> entities);
  }
}