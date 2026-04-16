package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.product.entity.Variant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantRepository extends JpaRepository<Variant, Long> {
  List<Variant> findAllByProductId(Long productId);

  Optional<Variant> findByIdAndProductId(Long variantId, Long productId);
}
