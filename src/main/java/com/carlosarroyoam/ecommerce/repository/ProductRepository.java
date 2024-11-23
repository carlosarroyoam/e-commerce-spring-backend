package com.carlosarroyoam.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.ecommerce.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
