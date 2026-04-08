package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse.CustomerAddressResponseMapper;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse.CustomerResponseMapper;
import com.carlosarroyoam.ecommerce.order.dto.OrderPaymentStatusResponse.OrderPaymentStatusResponseMapper;
import com.carlosarroyoam.ecommerce.order.dto.OrderStatusResponse.OrderStatusResponseMapper;
import com.carlosarroyoam.ecommerce.order.entity.Order;
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
public class OrderResponse {
  private Long id;
  private String orderNumber;
  private CustomerResponse customer;
  private CustomerAddressResponse shippingAddress;
  private OrderStatusResponse status;
  private OrderPaymentStatusResponse paymentStatus;
  private BigDecimal subtotal;
  private BigDecimal taxTotal;
  private BigDecimal shippingTotal;
  private BigDecimal total;
  private String notes;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      CustomerResponseMapper.class, CustomerAddressResponseMapper.class,
      OrderStatusResponseMapper.class, OrderPaymentStatusResponseMapper.class })
  public interface OrderResponseMapper {
    OrderResponseMapper INSTANCE = Mappers.getMapper(OrderResponseMapper.class);

    OrderResponse toDto(Order entity);

    List<OrderResponse> toDtos(List<Order> entities);
  }
}