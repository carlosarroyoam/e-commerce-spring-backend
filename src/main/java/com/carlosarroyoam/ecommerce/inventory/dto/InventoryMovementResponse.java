package com.carlosarroyoam.ecommerce.inventory.dto;

import com.carlosarroyoam.ecommerce.inventory.dto.MovementResponse.MovementResponseMapper;
import com.carlosarroyoam.ecommerce.inventory.entity.InventoryMovement;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse.VariantResponseMapper;
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
public class InventoryMovementResponse {
  private Long id;
  private Integer quantity;
  private VariantResponse variant;
  private MovementResponse movement;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {VariantResponseMapper.class, MovementResponseMapper.class})
  public interface InventoryMovementResponseMapper {
    InventoryMovementResponseMapper INSTANCE =
        Mappers.getMapper(InventoryMovementResponseMapper.class);

    InventoryMovementResponse toDto(InventoryMovement entity);

    List<InventoryMovementResponse> toDtos(List<InventoryMovement> entities);
  }
}
