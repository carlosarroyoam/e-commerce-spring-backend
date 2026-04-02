package com.carlosarroyoam.ecommerce.product.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductFilterDto {
  @Size(max = 96, message = "Title should be max 96")
  private String title;
}
