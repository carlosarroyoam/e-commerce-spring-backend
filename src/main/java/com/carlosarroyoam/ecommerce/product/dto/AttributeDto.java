package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.entity.Attribute;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttributeDto {
  private Long id;
  private String name;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface AttributeDtoMapper {
    AttributeDtoMapper INSTANCE = Mappers.getMapper(AttributeDtoMapper.class);

    @Mapping(source = "title", target = "name")
    AttributeDto toDto(Attribute entity);

    List<AttributeDto> toDtos(List<Attribute> entities);
  }
}