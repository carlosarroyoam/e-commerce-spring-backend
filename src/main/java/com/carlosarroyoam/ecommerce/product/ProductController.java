package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.product.entity.Product;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
  public ResponseEntity<List<Product>> findAll(
      @PageableDefault(page = 0, size = 25) Pageable pageable) {
    List<Product> products = productService.findAll(pageable);
    return ResponseEntity.ok(products);
  }

  @GetMapping(value = "/{productId}", produces = "application/json")
  public ResponseEntity<Product> findById(@PathVariable Long productId) {
    return ResponseEntity.ok(productService.findById(productId));
  }
}
