package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.entity.Product;
import java.time.LocalDateTime;
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

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ProductSummaryResponseMapper {
    ProductSummaryResponseMapper INSTANCE = Mappers.getMapper(ProductSummaryResponseMapper.class);

    ProductSummaryResponse toDto(Product entity);

    List<ProductSummaryResponse> toDtos(List<Product> entities);
  }
}
