package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.dto.AttributeResponse.AttributeResponseMapper;
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
public class VariantAttributeValueResponse {
  private Long id;
  private String value;
  private AttributeResponse attribute;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      AttributeResponseMapper.class })
  public interface VariantAttributeValueResponseMapper {
    VariantAttributeValueResponseMapper INSTANCE = Mappers
        .getMapper(VariantAttributeValueResponseMapper.class);

    VariantAttributeValueResponse toDto(VariantAttributeValue entity);

    List<VariantAttributeValueResponse> toDtos(List<VariantAttributeValue> entities);
  }
}