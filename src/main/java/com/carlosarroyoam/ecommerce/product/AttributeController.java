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

@RestController
@RequestMapping("/attributes")
public class AttributeController {
  private final AttributeService attributeService;

  public AttributeController(final AttributeService attributeService) {
    this.attributeService = attributeService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<AttributeResponse>> findAll(
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<AttributeResponse> attributes = attributeService.findAll(pageable);
    return ResponseEntity.ok(attributes);
  }

  @GetMapping(value = "/{attributeId}", produces = "application/json")
  public ResponseEntity<AttributeResponse> findById(@PathVariable Long attributeId) {
    AttributeResponse attributeById = attributeService.findById(attributeId);
    return ResponseEntity.ok(attributeById);
  }
}
