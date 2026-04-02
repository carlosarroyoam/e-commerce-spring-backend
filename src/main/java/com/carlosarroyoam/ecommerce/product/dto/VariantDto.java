package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.entity.Variant;
import com.carlosarroyoam.ecommerce.product.entity.VariantAttributeValue;
import com.carlosarroyoam.ecommerce.product.entity.VariantImage;
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
  private List<VariantAttributeValue> attributes;
  private List<VariantImage> images;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface VariantDtoMapper {
    VariantDtoMapper INSTANCE = Mappers.getMapper(VariantDtoMapper.class);

    VariantDto toDto(Variant entity);

    List<VariantDto> toDtos(List<Variant> entities);
  }
}