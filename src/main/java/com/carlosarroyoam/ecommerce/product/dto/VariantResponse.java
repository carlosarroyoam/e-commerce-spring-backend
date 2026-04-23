package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.dto.VariantAttributeValueResponse.VariantAttributeValueResponseMapper;
import com.carlosarroyoam.ecommerce.product.dto.VariantImageResponse.VariantImageResponseMapper;
import com.carlosarroyoam.ecommerce.product.entity.Variant;
import java.math.BigDecimal;
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
public class VariantResponse {
  private Long id;
  private String sku;
  private BigDecimal price;
  private BigDecimal comparedAtPrice;
  private List<VariantAttributeValueResponse> attributes;
  private List<VariantImageResponse> images;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {VariantAttributeValueResponseMapper.class, VariantImageResponseMapper.class})
  public interface VariantResponseMapper {
    VariantResponseMapper INSTANCE = Mappers.getMapper(VariantResponseMapper.class);

    VariantResponse toDto(Variant entity);

    List<VariantResponse> toDtos(List<Variant> entities);
  }
}
