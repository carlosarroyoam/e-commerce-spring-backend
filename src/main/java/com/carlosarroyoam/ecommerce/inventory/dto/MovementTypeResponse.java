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
public class MovementTypeResponse {
  private Byte id;
  private String title;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface MovementTypeResponseMapper {
    MovementTypeResponseMapper INSTANCE = Mappers.getMapper(MovementTypeResponseMapper.class);

    MovementTypeResponse toDto(MovementType entity);

    List<MovementTypeResponse> toDtos(List<MovementType> entities);
  }
}
