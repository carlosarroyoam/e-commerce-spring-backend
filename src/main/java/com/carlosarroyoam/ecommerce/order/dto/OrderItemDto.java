package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.order.dto.OrderDto.OrderDtoMapper;
import com.carlosarroyoam.ecommerce.order.entity.OrderItem;
import com.carlosarroyoam.ecommerce.product.dto.ProductDto;
import com.carlosarroyoam.ecommerce.product.dto.ProductDto.ProductDtoMapper;
import com.carlosarroyoam.ecommerce.product.dto.VariantDto;
import com.carlosarroyoam.ecommerce.product.dto.VariantDto.VariantDtoMapper;
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
public class OrderItemDto {
  private Long id;
  private OrderDto order;
  private ProductDto product;
  private VariantDto variant;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal total;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      OrderDtoMapper.class, ProductDtoMapper.class, VariantDtoMapper.class })
  public interface OrderItemDtoMapper {
    OrderItemDtoMapper INSTANCE = Mappers.getMapper(OrderItemDtoMapper.class);

    OrderItemDto toDto(OrderItem entity);

    List<OrderItemDto> toDtos(List<OrderItem> entities);
  }
}