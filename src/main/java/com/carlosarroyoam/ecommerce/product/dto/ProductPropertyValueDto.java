package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.entity.Attribute;
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
public class ProductPropertyValueDto {
  private Long id;
  private String value;
  private Attribute attribute;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ProductPropertyValueDtoMapper {
    ProductPropertyValueDtoMapper INSTANCE = Mappers.getMapper(ProductPropertyValueDtoMapper.class);

    ProductPropertyValueDto toDto(ProductPropertyValue entity);

    List<ProductPropertyValueDto> toDtos(List<ProductPropertyValue> entities);
  }
}