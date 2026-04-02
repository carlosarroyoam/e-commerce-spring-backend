package com.carlosarroyoam.ecommerce.inventory.dto;

import com.carlosarroyoam.ecommerce.inventory.dto.MovementDto.MovementDtoMapper;
import com.carlosarroyoam.ecommerce.inventory.entity.InventoryMovement;
import com.carlosarroyoam.ecommerce.product.dto.VariantDto;
import com.carlosarroyoam.ecommerce.product.dto.VariantDto.VariantDtoMapper;
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
public class InventoryMovementDto {
  private Long id;
  private Integer quantity;
  private VariantDto variant;
  private MovementDto movement;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      VariantDtoMapper.class, MovementDtoMapper.class })
  public interface InventoryMovementDtoMapper {
    InventoryMovementDtoMapper INSTANCE = Mappers.getMapper(InventoryMovementDtoMapper.class);

    InventoryMovementDto toDto(InventoryMovement entity);

    List<InventoryMovementDto> toDtos(List<InventoryMovement> entities);
  }
}