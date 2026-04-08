package com.carlosarroyoam.ecommerce.product.dto;

import com.carlosarroyoam.ecommerce.product.entity.Attribute;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttributeResponse {
  private Long id;
  private String name;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface AttributeResponseMapper {
    AttributeResponseMapper INSTANCE = Mappers.getMapper(AttributeResponseMapper.class);

    @Mapping(source = "title", target = "name")
    AttributeResponse toDto(Attribute entity);

    List<AttributeResponse> toDtos(List<Attribute> entities);
  }
}