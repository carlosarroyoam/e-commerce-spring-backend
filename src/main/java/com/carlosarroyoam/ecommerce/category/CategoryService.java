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
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoryService {
  private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
  private final CategoryRepository categoryRepository;

  public CategoryService(final CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public PagedResponse<CategoryResponse> findAll(Pageable pageable) {
    Page<Category> categories = categoryRepository.findAll(pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(categories.map(CategoryResponseMapper.INSTANCE::toDto));
  }

  public CategoryResponse findById(Byte categoryId) {
    Category categoryById = findCategoryByIdOrFail(categoryId);
    return CategoryResponseMapper.INSTANCE.toDto(categoryById);
  }

  private Category findCategoryByIdOrFail(Byte categoryId) {
    return categoryRepository.findById(categoryId).orElseThrow(() -> {
      log.warn(AppMessages.CATEGORY_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.CATEGORY_NOT_FOUND_EXCEPTION);
    });
  }
}
