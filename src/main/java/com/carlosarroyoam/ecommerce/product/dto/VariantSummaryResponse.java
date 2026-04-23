package com.carlosarroyoam.ecommerce.product.dto;

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
public class VariantSummaryResponse {
  private Long id;
  private String sku;
  private BigDecimal price;
  private BigDecimal comparedAtPrice;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface VariantSummaryResponseMapper {
    VariantSummaryResponseMapper INSTANCE = Mappers.getMapper(VariantSummaryResponseMapper.class);

    VariantSummaryResponse toDto(Variant entity);

    List<VariantSummaryResponse> toDtos(List<Variant> entities);
  }
}
