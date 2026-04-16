package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.product.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}
