package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.entity.Property;
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
public class PropertyDto {
  private Long id;
  private String title;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface PropertyDtoMapper {
    PropertyDtoMapper INSTANCE = Mappers.getMapper(PropertyDtoMapper.class);

    PropertyDto toDto(Property entity);

    List<PropertyDto> toDtos(List<Property> entities);
  }
}