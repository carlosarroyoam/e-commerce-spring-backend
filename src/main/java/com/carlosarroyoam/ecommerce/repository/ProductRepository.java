package com.carlosarroyoam.ecommerce.repository;

import com.carlosarroyoam.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
