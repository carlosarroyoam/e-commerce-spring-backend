package com.carlosarroyoam.ecommerce.refund.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderItemResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderItemResponse.OrderItemResponseMapper;
import com.carlosarroyoam.ecommerce.refund.entity.RefundItem;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Getter
@Setter
@Builder
public class RefundItemResponse {
  private Long id;
  private Integer quantity;
  private BigDecimal amount;
  private OrderItemResponse orderItem;
  private Long refundId;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {OrderItemResponseMapper.class})
  public interface RefundItemResponseMapper {
    RefundItemResponseMapper INSTANCE = Mappers.getMapper(RefundItemResponseMapper.class);

    @Mapping(source = "refund.id", target = "refundId")
    RefundItemResponse toDto(RefundItem entity);

    List<RefundItemResponse> toDtos(List<RefundItem> entities);
  }
}
