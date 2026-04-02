package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.entity.Category;
import com.carlosarroyoam.ecommerce.product.entity.Product;
import com.carlosarroyoam.ecommerce.product.entity.ProductPropertyValue;
import com.carlosarroyoam.ecommerce.product.entity.Variant;
import java.time.LocalDateTime;
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
public class ProductDto {
  private Long id;
  private String title;
  private String slug;
  private String description;
  private boolean featured;
  private boolean active;
  private CategoryDto category;
  private List<ProductPropertyValueDto> properties;
  private List<VariantDto> variants;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ProductDtoMapper {
    ProductDtoMapper INSTANCE = Mappers.getMapper(ProductDtoMapper.class);

    ProductDto toDto(Product entity);

    List<ProductDto> toDtos(List<Product> entities);
  }
}
