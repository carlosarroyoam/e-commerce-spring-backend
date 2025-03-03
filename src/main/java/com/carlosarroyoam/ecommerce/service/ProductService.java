package com.carlosarroyoam.ecommerce.service;

import com.carlosarroyoam.ecommerce.constant.AppMessages;
import com.carlosarroyoam.ecommerce.entity.Product;
import com.carlosarroyoam.ecommerce.repository.ProductRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {
  private static final Logger log = LoggerFactory.getLogger(ProductService.class);
  private final ProductRepository productRepository;

  public ProductService(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public List<Product> findAll(Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Product> products = productRepository.findAll(pageable);
    return products.getContent();
  }

  public Product findById(Long productId) {
    return productRepository.findById(productId).orElseThrow(() -> {
      log.warn(AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
    });
  }
}
