package com.carlosarroyoam.demojpa.product;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {
	private final Logger logger = LoggerFactory.getLogger(ProductController.class);

	@Autowired
	private ProductService productService;

	@GetMapping(produces = "application/json")
	public List<Product> findAll() {
		return productService.findAll();
	}

	@GetMapping(value = "/{productId}", produces = "application/json")
	public Optional<Product> findById(@PathVariable Long productId) {
		return productService.findById(productId);
	}
}
