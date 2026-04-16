package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.product.dto.PropertyResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/properties")
public class PropertyController {
  private final PropertyService propertyService;

  public PropertyController(final PropertyService propertyService) {
    this.propertyService = propertyService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<PropertyResponse>> findAll(
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<PropertyResponse> properties = propertyService.findAll(pageable);
    return ResponseEntity.ok(properties);
  }

  @GetMapping(value = "/{propertyId}", produces = "application/json")
  public ResponseEntity<PropertyResponse> findById(@PathVariable Long propertyId) {
    PropertyResponse propertyById = propertyService.findById(propertyId);
    return ResponseEntity.ok(propertyById);
  }
}
