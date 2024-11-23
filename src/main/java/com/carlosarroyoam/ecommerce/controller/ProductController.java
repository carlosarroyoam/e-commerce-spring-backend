package com.carlosarroyoam.ecommerce.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carlosarroyoam.ecommerce.entity.Product;
import com.carlosarroyoam.ecommerce.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {
	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping(produces = "application/json")
	public List<Product> findAll() {
		return productService.findAll();
	}

	@GetMapping(value = "/{productId}", produces = "application/json")
	public Optional<Product> findById(@PathVariable Long productId) {
		return productService.findById(productId);
	}
}
