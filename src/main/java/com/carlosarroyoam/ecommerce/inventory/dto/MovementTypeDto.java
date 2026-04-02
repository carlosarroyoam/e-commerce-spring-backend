package com.carlosarroyoam.ecommerce.inventory.dto;

import com.carlosarroyoam.ecommerce.inventory.entity.MovementType;
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
public class MovementTypeDto {
  private Byte id;
  private String title;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface MovementTypeDtoMapper {
    MovementTypeDtoMapper INSTANCE = Mappers.getMapper(MovementTypeDtoMapper.class);

    MovementTypeDto toDto(MovementType entity);

    List<MovementTypeDto> toDtos(List<MovementType> entities);
  }
}