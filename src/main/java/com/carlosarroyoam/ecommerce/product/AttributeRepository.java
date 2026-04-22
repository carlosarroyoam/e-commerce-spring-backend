package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.product.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {}
