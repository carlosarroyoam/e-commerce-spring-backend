package com.carlosarroyoam.ecommerce.refund.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderItemResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderItemResponse.OrderItemResponseMapper;
import com.carlosarroyoam.ecommerce.refund.dto.RefundResponse.RefundResponseMapper;
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
public class RefundItemResponse {
  private Long id;
  private Integer quantity;
  private BigDecimal amount;
  private OrderItemResponse orderItem;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      RefundResponseMapper.class, OrderItemResponseMapper.class })
  public interface RefundItemResponseMapper {
    RefundItemResponseMapper INSTANCE = Mappers.getMapper(RefundItemResponseMapper.class);

    RefundItemResponse toDto(RefundItem entity);

    List<RefundItemResponse> toDtos(List<RefundItem> entities);
  }
}