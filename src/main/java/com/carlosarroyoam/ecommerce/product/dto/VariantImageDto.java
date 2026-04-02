package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.entity.VariantImage;
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
public class VariantImageDto {
  private Long id;
  private String url;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface VariantImageDtoMapper {
    VariantImageDtoMapper INSTANCE = Mappers.getMapper(VariantImageDtoMapper.class);

    VariantImageDto toDto(VariantImage entity);

    List<VariantImageDto> toDtos(List<VariantImage> entities);
  }
}