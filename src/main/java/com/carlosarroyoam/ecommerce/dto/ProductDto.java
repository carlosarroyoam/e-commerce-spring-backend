package com.carlosarroyoam.ecommerce.dto;

import com.carlosarroyoam.ecommerce.entity.ProductPropertyValue;
import com.carlosarroyoam.ecommerce.entity.Variant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {
  private Long id;
  private String title;
  private String slug;
  private String description;
  private boolean featured;
  private boolean active;
  private String category;
  private List<ProductPropertyValue> properties;
  private List<Variant> variants;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
}
