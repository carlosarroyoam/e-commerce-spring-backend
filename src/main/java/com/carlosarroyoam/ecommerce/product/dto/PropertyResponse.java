package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.entity.Property;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/** Representa una propiedad descriptiva de producto (por ejemplo, material). */
@Getter
@Setter
@Builder
public class PropertyResponse {
  private Long id;
  private String title;
  private LocalDateTime deletedAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface PropertyResponseMapper {
    PropertyResponseMapper INSTANCE = Mappers.getMapper(PropertyResponseMapper.class);

    PropertyResponse toDto(Property entity);

    List<PropertyResponse> toDtos(List<Property> entities);
  }
}
