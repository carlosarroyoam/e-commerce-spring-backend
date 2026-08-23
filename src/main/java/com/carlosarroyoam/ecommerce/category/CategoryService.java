package com.carlosarroyoam.ecommerce.category;

import com.carlosarroyoam.ecommerce.category.dto.CategoryResponse;
import com.carlosarroyoam.ecommerce.category.dto.CategoryResponse.CategoryResponseMapper;
import com.carlosarroyoam.ecommerce.category.entity.Category;
import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Lógica de negocio para consultar {@link Category}. */
@Service
public class CategoryService {
  private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
  private final CategoryRepository categoryRepository;

  public CategoryService(final CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  /**
   * Obtiene una página de categorías.
   *
   * @param pageable la paginación y el orden a aplicar
   * @return la página de {@link CategoryResponse} resultante
   */
  @Transactional(readOnly = true)
  public PagedResponse<CategoryResponse> findAll(Pageable pageable) {
    Page<Category> categories = categoryRepository.findAll(pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        categories.map(CategoryResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca una categoría por su id.
   *
   * @param categoryId el id de la categoría
   * @return el {@link CategoryResponse} correspondiente
   * @throws ResponseStatusException con 404 si no existe una categoría con ese id
   */
  @Transactional(readOnly = true)
  public CategoryResponse findById(Byte categoryId) {
    Category categoryById = findCategoryByIdOrFail(categoryId);
    return CategoryResponseMapper.INSTANCE.toDto(categoryById);
  }

  /**
   * Busca una categoría por su id o lanza una excepción si no existe.
   *
   * @param categoryId el id de la categoría
   * @return la {@link Category} encontrada
   * @throws ResponseStatusException con 404 si no existe una categoría con ese id
   */
  private Category findCategoryByIdOrFail(Byte categoryId) {
    return categoryRepository
        .findById(categoryId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.CATEGORY_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.CATEGORY_NOT_FOUND_EXCEPTION);
            });
  }
}
