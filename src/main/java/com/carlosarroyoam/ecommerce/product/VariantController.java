package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.product.dto.VariantResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints REST de consulta de variantes de un producto bajo {@code
 * /products/{productId}/variants}.
 */
@RestController
@RequestMapping("/products/{productId}/variants")
public class VariantController {
  private final VariantService variantService;

  public VariantController(final VariantService variantService) {
    this.variantService = variantService;
  }

  /**
   * Lista todas las variantes de un producto.
   *
   * @param productId el id del producto
   * @return la lista de variantes y el estado 200 OK
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<List<VariantResponse>> findAllByProductId(@PathVariable Long productId) {
    List<VariantResponse> variants = variantService.findAllByProductId(productId);
    return ResponseEntity.ok(variants);
  }

  /**
   * Obtiene una variante de un producto por su id.
   *
   * @param productId el id del producto
   * @param variantId el id de la variante
   * @return la variante encontrada y el estado 200 OK
   */
  @GetMapping(value = "/{variantId}", produces = "application/json")
  public ResponseEntity<VariantResponse> findById(
      @PathVariable Long productId, @PathVariable Long variantId) {
    VariantResponse variantById = variantService.findById(productId, variantId);
    return ResponseEntity.ok(variantById);
  }
}
