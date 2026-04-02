package com.carlosarroyoam.ecommerce.refund.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderDto;
import com.carlosarroyoam.ecommerce.order.dto.OrderDto.OrderDtoMapper;
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
public class RefundDto {
  private Long id;
  private BigDecimal amount;
  private OrderDto order;
  private String reason;
  private LocalDateTime createdAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      OrderDtoMapper.class })
  public interface RefundDtoMapper {
    RefundDtoMapper INSTANCE = Mappers.getMapper(RefundDtoMapper.class);

    RefundDto toDto(Refund entity);

    List<RefundDto> toDtos(List<Refund> entities);
  }
}