package com.carlosarroyoam.ecommerce.category.dto;

import com.carlosarroyoam.ecommerce.category.entity.Category;
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
public class CategoryResponse {
  private Byte id;
  private String title;
  private LocalDateTime deletedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface CategoryResponseMapper {
    CategoryResponseMapper INSTANCE = Mappers.getMapper(CategoryResponseMapper.class);

    CategoryResponse toDto(Category entity);

    List<CategoryResponse> toDtos(List<Category> entities);
  }
}