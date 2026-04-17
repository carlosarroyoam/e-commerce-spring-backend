package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.dto.PropertyResponse.PropertyResponseMapper;
import com.carlosarroyoam.ecommerce.product.entity.ProductPropertyValue;
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
public class ProductPropertyValueResponse {
  private Long id;
  private String value;
  private PropertyResponse property;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      PropertyResponseMapper.class })
  public interface ProductPropertyValueResponseMapper {
    ProductPropertyValueResponseMapper INSTANCE = Mappers
        .getMapper(ProductPropertyValueResponseMapper.class);

    ProductPropertyValueResponse toDto(ProductPropertyValue entity);

    List<ProductPropertyValueResponse> toDtos(List<ProductPropertyValue> entities);
  }
}