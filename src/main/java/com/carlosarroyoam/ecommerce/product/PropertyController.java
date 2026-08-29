package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.product.dto.PropertyResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone los endpoints REST de consulta de propiedades de producto bajo {@code /properties}. */
@RestController
@RequestMapping("/properties")
public class PropertyController {
  private final PropertyService propertyService;

  public PropertyController(final PropertyService propertyService) {
    this.propertyService = propertyService;
  }

  /**
   * Lista las propiedades de forma paginada.
   *
   * @param pageable la paginación y el orden a aplicar
   * @return la página de propiedades y el estado 200 OK
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<PropertyResponse>> findAll(
      @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
    PagedResponse<PropertyResponse> properties = propertyService.findAll(pageable);
    return ResponseEntity.ok(properties);
  }

  /**
   * Obtiene una propiedad por su id.
   *
   * @param propertyId el id de la propiedad
   * @return la propiedad encontrada y el estado 200 OK
   */
  @GetMapping(value = "/{propertyId}", produces = "application/json")
  public ResponseEntity<PropertyResponse> findById(@PathVariable Long propertyId) {
    PropertyResponse propertyById = propertyService.findById(propertyId);
    return ResponseEntity.ok(propertyById);
  }
}
