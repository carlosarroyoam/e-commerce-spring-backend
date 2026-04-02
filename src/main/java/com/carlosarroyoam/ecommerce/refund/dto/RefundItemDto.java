package com.carlosarroyoam.ecommerce.refund.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderItemDto;
import com.carlosarroyoam.ecommerce.order.dto.OrderItemDto.OrderItemDtoMapper;
import com.carlosarroyoam.ecommerce.refund.dto.RefundDto.RefundDtoMapper;
import com.carlosarroyoam.ecommerce.refund.entity.RefundItem;
import java.math.BigDecimal;
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
public class RefundItemDto {
  private Long id;
  private Integer quantity;
  private BigDecimal amount;
  private RefundDto refund;
  private OrderItemDto orderItem;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      RefundDtoMapper.class, OrderItemDtoMapper.class })
  public interface RefundItemDtoMapper {
    RefundItemDtoMapper INSTANCE = Mappers.getMapper(RefundItemDtoMapper.class);

    RefundItemDto toDto(RefundItem entity);

    List<RefundItemDto> toDtos(List<RefundItem> entities);
  }
}