package com.carlosarroyoam.ecommerce.category;

import com.carlosarroyoam.ecommerce.category.dto.CategoryResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone los endpoints REST de consulta de categorías bajo {@code /categories}. */
@RestController
@RequestMapping("/categories")
public class CategoryController {
  private final CategoryService categoryService;

  public CategoryController(final CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  /**
   * Lista las categorías de forma paginada.
   *
   * @param pageable la paginación y el orden a aplicar
   * @return la página de categorías y el estado 200 OK
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<CategoryResponse>> findAll(
      @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
    PagedResponse<CategoryResponse> categories = categoryService.findAll(pageable);
    return ResponseEntity.ok(categories);
  }

  /**
   * Obtiene una categoría por su id.
   *
   * @param categoryId el id de la categoría
   * @return la categoría encontrada y el estado 200 OK
   */
  @GetMapping(value = "/{categoryId}", produces = "application/json")
  public ResponseEntity<CategoryResponse> findById(@PathVariable Byte categoryId) {
    CategoryResponse categoryById = categoryService.findById(categoryId);
    return ResponseEntity.ok(categoryById);
  }
}
