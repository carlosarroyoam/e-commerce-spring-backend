package com.carlosarroyoam.ecommerce.inventory.dto;

import com.carlosarroyoam.ecommerce.inventory.entity.MovementType;
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
