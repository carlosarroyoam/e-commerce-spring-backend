package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressDto;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressDto.CustomerAddressDtoMapper;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerDto;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerDto.CustomerDtoMapper;
import com.carlosarroyoam.ecommerce.order.dto.OrderPaymentStatusDto.OrderPaymentStatusDtoMapper;
import com.carlosarroyoam.ecommerce.order.dto.OrderStatusDto.OrderStatusDtoMapper;
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
public class OrderDto {
  private Long id;
  private String orderNumber;
  private CustomerDto customer;
  private CustomerAddressDto shippingAddress;
  private OrderStatusDto status;
  private OrderPaymentStatusDto paymentStatus;
  private BigDecimal subtotal;
  private BigDecimal taxTotal;
  private BigDecimal shippingTotal;
  private BigDecimal total;
  private String notes;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      CustomerDtoMapper.class, CustomerAddressDtoMapper.class, OrderStatusDtoMapper.class,
      OrderPaymentStatusDtoMapper.class })
  public interface OrderDtoMapper {
    OrderDtoMapper INSTANCE = Mappers.getMapper(OrderDtoMapper.class);

    OrderDto toDto(Order entity);

    List<OrderDto> toDtos(List<Order> entities);
  }
}