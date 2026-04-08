package com.carlosarroyoam.ecommerce.refund.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderResponse;
import com.carlosarroyoam.ecommerce.order.dto.OrderResponse.OrderResponseMapper;
import com.carlosarroyoam.ecommerce.refund.entity.Refund;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class RefundResponse {
  private Long id;
  private BigDecimal amount;
  private OrderResponse order;
  private String reason;
  private LocalDateTime createdAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      OrderResponseMapper.class })
  public interface RefundResponseMapper {
    RefundResponseMapper INSTANCE = Mappers.getMapper(RefundResponseMapper.class);

    RefundResponse toDto(Refund entity);

    List<RefundResponse> toDtos(List<Refund> entities);
  }
}