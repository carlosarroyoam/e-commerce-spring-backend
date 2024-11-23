package com.carlosarroyoam.ecommerce.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.carlosarroyoam.ecommerce.entity.Product;
import com.carlosarroyoam.ecommerce.repository.ProductRepository;

@Service
public class ProductService {
	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public List<Product> findAll() {
		return productRepository.findAll();
	}

	public Optional<Product> findById(Long productId) {
		return productRepository.findById(productId);
	}
}
