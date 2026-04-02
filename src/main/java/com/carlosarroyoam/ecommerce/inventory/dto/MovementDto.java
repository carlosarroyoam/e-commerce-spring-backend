package com.carlosarroyoam.ecommerce.inventory.dto;

import com.carlosarroyoam.ecommerce.inventory.dto.MovementTypeDto.MovementTypeDtoMapper;
import com.carlosarroyoam.ecommerce.inventory.entity.Movement;
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
public class MovementDto {
  private Byte id;
  private String title;
  private MovementTypeDto movementType;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      MovementTypeDtoMapper.class })
  public interface MovementDtoMapper {
    MovementDtoMapper INSTANCE = Mappers.getMapper(MovementDtoMapper.class);

    MovementDto toDto(Movement entity);

    List<MovementDto> toDtos(List<Movement> entities);
  }
}