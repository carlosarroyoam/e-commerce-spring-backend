package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.product.dto.AttributeResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone los endpoints REST de consulta de atributos de producto bajo {@code /attributes}. */
@RestController
@RequestMapping("/attributes")
public class AttributeController {
  private final AttributeService attributeService;

  public AttributeController(final AttributeService attributeService) {
    this.attributeService = attributeService;
  }

  /**
   * Lista los atributos de forma paginada.
   *
   * @param pageable la paginación y el orden a aplicar
   * @return la página de atributos y el estado 200 OK
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<AttributeResponse>> findAll(
      @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
    PagedResponse<AttributeResponse> attributes = attributeService.findAll(pageable);
    return ResponseEntity.ok(attributes);
  }

  /**
   * Obtiene un atributo por su id.
   *
   * @param attributeId el id del atributo
   * @return el atributo encontrado y el estado 200 OK
   */
  @GetMapping(value = "/{attributeId}", produces = "application/json")
  public ResponseEntity<AttributeResponse> findById(@PathVariable Long attributeId) {
    AttributeResponse attributeById = attributeService.findById(attributeId);
    return ResponseEntity.ok(attributeById);
  }
}
