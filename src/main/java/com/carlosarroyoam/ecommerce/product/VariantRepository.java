package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.product.entity.Variant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos para la entidad {@link Variant}. */
public interface VariantRepository extends JpaRepository<Variant, Long> {
  /**
   * Lista todas las variantes de un producto.
   *
   * @param productId el id del producto
   * @return las variantes del producto
   */
  List<Variant> findAllByProductId(Long productId);

  /**
   * Busca una variante por su id, verificando que pertenezca al producto indicado.
   *
   * @param variantId el id de la variante
   * @param productId el id del producto al que debe pertenecer
   * @return la variante encontrada, o {@link Optional#empty()} si no existe o no pertenece a ese
   *     producto
   */
  Optional<Variant> findByIdAndProductId(Long variantId, Long productId);
}
