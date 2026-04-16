package com.carlosarroyoam.ecommerce.payment.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderResponse.OrderResponseMapper;
import com.carlosarroyoam.ecommerce.payment.entity.Payment;
import com.carlosarroyoam.ecommerce.payment.entity.PaymentMethod;
import com.carlosarroyoam.ecommerce.payment.entity.PaymentStatus;
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
public class PaymentResponse {
  private Long id;
  private BigDecimal amount;
  private String reference;
  private String description;
  private PaymentMethod method;
  private PaymentStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      OrderResponseMapper.class })
  public interface PaymentResponseMapper {
    PaymentResponseMapper INSTANCE = Mappers.getMapper(PaymentResponseMapper.class);

    PaymentResponse toDto(Payment entity);

    List<PaymentResponse> toDtos(List<Payment> entities);
  }
}
