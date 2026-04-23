package com.carlosarroyoam.ecommerce.refund.dto;

import com.carlosarroyoam.ecommerce.refund.dto.RefundItemResponse.RefundItemResponseMapper;
import com.carlosarroyoam.ecommerce.refund.entity.Refund;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class RefundResponse {
  private Long id;
  private BigDecimal amount;
  private String reason;
  private List<RefundItemResponse> items;
  private Long orderId;
  private LocalDateTime createdAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {RefundItemResponseMapper.class})
  public interface RefundResponseMapper {
    RefundResponseMapper INSTANCE = Mappers.getMapper(RefundResponseMapper.class);

    @Mapping(source = "order.id", target = "orderId")
    RefundResponse toDto(Refund entity);

    List<RefundResponse> toDtos(List<Refund> entities);
  }
}
