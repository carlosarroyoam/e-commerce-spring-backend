package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.category.dto.CategoryDto;
import com.carlosarroyoam.ecommerce.category.dto.CategoryDto.CategoryDtoMapper;
import com.carlosarroyoam.ecommerce.product.dto.ProductPropertyValueDto.ProductPropertyValueDtoMapper;
import com.carlosarroyoam.ecommerce.product.dto.VariantDto.VariantDtoMapper;
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
public class ProductDto {
  private Long id;
  private String title;
  private String slug;
  private String description;
  private Boolean isFeatured;
  private Boolean isActive;
  private CategoryDto category;
  private List<ProductPropertyValueDto> properties;
  private List<VariantDto> variants;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      CategoryDtoMapper.class, ProductPropertyValueDtoMapper.class, VariantDtoMapper.class })
  public interface ProductDtoMapper {
    ProductDtoMapper INSTANCE = Mappers.getMapper(ProductDtoMapper.class);

    ProductDto toDto(Product entity);

    List<ProductDto> toDtos(List<Product> entities);
  }
}
