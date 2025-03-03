package com.carlosarroyoam.ecommerce.controller;

import com.carlosarroyoam.ecommerce.entity.Product;
import com.carlosarroyoam.ecommerce.service.ProductService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "25") Integer size) {
    List<Product> products = productService.findAll(page, size);
    return ResponseEntity.ok(products);
  }

  @GetMapping(value = "/{productId}", produces = "application/json")
  public ResponseEntity<Product> findById(@PathVariable Long productId) {
    return ResponseEntity.ok(productService.findById(productId));
  }
}
