package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.dto.AttributeDto.AttributeDtoMapper;
import com.carlosarroyoam.ecommerce.product.entity.VariantAttributeValue;
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
public class VariantAttributeValueDto {
  private Long id;
  private String value;
  private AttributeDto attribute;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      AttributeDtoMapper.class })
  public interface VariantAttributeValueDtoMapper {
    VariantAttributeValueDtoMapper INSTANCE = Mappers
        .getMapper(VariantAttributeValueDtoMapper.class);

    VariantAttributeValueDto toDto(VariantAttributeValue entity);

    List<VariantAttributeValueDto> toDtos(List<VariantAttributeValue> entities);
  }
}