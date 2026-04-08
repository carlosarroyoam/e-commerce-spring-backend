package com.carlosarroyoam.ecommerce.product.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder
public class ProductSpecs {
  @Size(max = 96, message = "Title should be max 96")
  private String title;

  @Size(max = 96, message = "Slug should be max 96")
  private String slug;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  private Boolean isFeatured;
  private Boolean isActive;
  private Byte categoryId;
}
