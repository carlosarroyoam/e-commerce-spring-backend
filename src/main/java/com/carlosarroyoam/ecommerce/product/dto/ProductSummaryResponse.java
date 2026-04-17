package com.carlosarroyoam.ecommerce.product.dto;

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
public class ProductSummaryResponse {
  private Long id;
  private String title;
  private String slug;
  private String description;
  private Boolean isFeatured;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      CategoryResponseMapper.class, ProductPropertyValueResponseMapper.class,
      VariantResponseMapper.class })
  public interface ProductResponseMapper {
    ProductResponseMapper INSTANCE = Mappers.getMapper(ProductResponseMapper.class);

    ProductSummaryResponse toDto(Product entity);

    List<ProductSummaryResponse> toDtos(List<Product> entities);
  }
}
