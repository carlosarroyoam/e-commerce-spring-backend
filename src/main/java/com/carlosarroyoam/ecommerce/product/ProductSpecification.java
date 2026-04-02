package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {
  private ProductSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Product> titleContains(String title) {
    return (product, cq, cb) -> {
      if (title == null || title.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(product.get("title")), "%" + title.toLowerCase() + "%");
    };
  }
}
