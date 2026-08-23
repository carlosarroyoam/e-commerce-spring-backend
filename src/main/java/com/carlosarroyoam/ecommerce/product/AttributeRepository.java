package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.product.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos para la entidad {@link Attribute}. */
public interface AttributeRepository extends JpaRepository<Attribute, Long> {}
