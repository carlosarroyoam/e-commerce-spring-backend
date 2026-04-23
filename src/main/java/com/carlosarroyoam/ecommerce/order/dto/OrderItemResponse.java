package com.carlosarroyoam.ecommerce.order.dto;

import com.carlosarroyoam.ecommerce.order.entity.OrderItem;
import com.carlosarroyoam.ecommerce.product.dto.ProductSummaryResponse;
import com.carlosarroyoam.ecommerce.product.dto.ProductSummaryResponse.ProductSummaryResponseMapper;
import com.carlosarroyoam.ecommerce.product.dto.VariantSummaryResponse;
import com.carlosarroyoam.ecommerce.product.dto.VariantSummaryResponse.VariantSummaryResponseMapper;
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
public class OrderItemResponse {
  private Long id;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal total;
  private ProductSummaryResponse product;
  private VariantSummaryResponse variant;
  private Long orderId;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {ProductSummaryResponseMapper.class, VariantSummaryResponseMapper.class})
  public interface OrderItemResponseMapper {
    OrderItemResponseMapper INSTANCE = Mappers.getMapper(OrderItemResponseMapper.class);

    @Mapping(source = "order.id", target = "orderId")
    OrderItemResponse toDto(OrderItem entity);

    List<OrderItemResponse> toDtos(List<OrderItem> entities);
  }
}
