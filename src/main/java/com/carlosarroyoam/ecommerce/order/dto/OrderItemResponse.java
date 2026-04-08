package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderResponse.OrderResponseMapper;
import com.carlosarroyoam.ecommerce.order.entity.OrderItem;
import com.carlosarroyoam.ecommerce.product.dto.ProductResponse;
import com.carlosarroyoam.ecommerce.product.dto.ProductResponse.ProductResponseMapper;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse.VariantResponseMapper;
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
public class OrderItemResponse {
  private Long id;
  private OrderResponse order;
  private ProductResponse product;
  private VariantResponse variant;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal total;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      OrderResponseMapper.class, ProductResponseMapper.class, VariantResponseMapper.class })
  public interface OrderItemResponseMapper {
    OrderItemResponseMapper INSTANCE = Mappers.getMapper(OrderItemResponseMapper.class);

    OrderItemResponse toDto(OrderItem entity);

    List<OrderItemResponse> toDtos(List<OrderItem> entities);
  }
}