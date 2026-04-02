package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.dto.VariantAttributeValueDto.VariantAttributeValueDtoMapper;
import com.carlosarroyoam.ecommerce.product.dto.VariantImageDto.VariantImageDtoMapper;
import com.carlosarroyoam.ecommerce.product.entity.Variant;
import java.math.BigDecimal;
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
public class VariantDto {
  private Long id;
  private String sku;
  private BigDecimal price;
  private BigDecimal comparedAtPrice;
  private BigDecimal costPerItem;
  private Integer quantityOnStock;
  private List<VariantAttributeValueDto> attributes;
  private List<VariantImageDto> images;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      VariantAttributeValueDtoMapper.class, VariantImageDtoMapper.class })
  public interface VariantDtoMapper {
    VariantDtoMapper INSTANCE = Mappers.getMapper(VariantDtoMapper.class);

    VariantDto toDto(Variant entity);

    List<VariantDto> toDtos(List<Variant> entities);
  }
}