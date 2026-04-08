package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.product.dto.ProductResponse;
import com.carlosarroyoam.ecommerce.product.dto.ProductSpecs;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {
  private final ProductService productService;

  public ProductController(final ProductService productService) {
    this.productService = productService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<ProductResponse>> findAll(
      @Valid @ModelAttribute ProductSpecs productSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<ProductResponse> products = productService.findAll(productSpecs, pageable);
    return ResponseEntity.ok(products);
  }

  @GetMapping(value = "/{productId}", produces = "application/json")
  public ResponseEntity<ProductResponse> findById(@PathVariable Long productId) {
    ProductResponse productById = productService.findById(productId);
    return ResponseEntity.ok(productById);
  }
}
