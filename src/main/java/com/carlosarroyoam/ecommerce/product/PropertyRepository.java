package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.product.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos para la entidad {@link Property}. */
public interface PropertyRepository extends JpaRepository<Property, Long> {}
