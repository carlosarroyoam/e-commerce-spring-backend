package com.carlosarroyoam.ecommerce.inventory.dto;

import com.carlosarroyoam.ecommerce.inventory.dto.MovementTypeResponse.MovementTypeResponseMapper;
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
public class MovementResponse {
  private Byte id;
  private String title;
  private MovementTypeResponse movementType;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {MovementTypeResponseMapper.class})
  public interface MovementResponseMapper {
    MovementResponseMapper INSTANCE = Mappers.getMapper(MovementResponseMapper.class);

    MovementResponse toDto(Movement entity);

    List<MovementResponse> toDtos(List<Movement> entities);
  }
}
