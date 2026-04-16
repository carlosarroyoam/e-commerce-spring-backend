package com.carlosarroyoam.ecommerce.category;

import com.carlosarroyoam.ecommerce.category.dto.CategoryResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {
  private final CategoryService categoryService;

  public CategoryController(final CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<CategoryResponse>> findAll(
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<CategoryResponse> categories = categoryService.findAll(pageable);
    return ResponseEntity.ok(categories);
  }

  @GetMapping(value = "/{categoryId}", produces = "application/json")
  public ResponseEntity<CategoryResponse> findById(@PathVariable Byte categoryId) {
    CategoryResponse categoryById = categoryService.findById(categoryId);
    return ResponseEntity.ok(categoryById);
  }
}
