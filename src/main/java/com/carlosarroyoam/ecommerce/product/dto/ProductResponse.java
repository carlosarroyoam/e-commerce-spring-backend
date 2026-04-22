package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.category.dto.CategoryResponse;
import com.carlosarroyoam.ecommerce.category.dto.CategoryResponse.CategoryResponseMapper;
import com.carlosarroyoam.ecommerce.product.dto.ProductPropertyValueResponse.ProductPropertyValueResponseMapper;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse.VariantResponseMapper;
import com.carlosarroyoam.ecommerce.product.entity.Product;
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
public class ProductResponse {
  private Long id;
  private String title;
  private String slug;
  private String description;
  private Boolean isFeatured;
  private Boolean isActive;
  private CategoryResponse category;
  private List<ProductPropertyValueResponse> properties;
  private List<VariantResponse> variants;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {
        CategoryResponseMapper.class,
        ProductPropertyValueResponseMapper.class,
        VariantResponseMapper.class
      })
  public interface ProductResponseMapper {
    ProductResponseMapper INSTANCE = Mappers.getMapper(ProductResponseMapper.class);

    ProductResponse toDto(Product entity);

    List<ProductResponse> toDtos(List<Product> entities);
  }
}
