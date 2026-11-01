package com.carlosarroyoam.ecommerce.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.category.dto.CategoryResponse;
import com.carlosarroyoam.ecommerce.category.entity.Category;
import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
  @Mock private CategoryRepository categoryRepository;
  @InjectMocks private CategoryService categoryService;

  private Category category;

  @BeforeEach
  void setUp() {
    category = Category.builder().id((byte) 1).title("Smartphones").slug("smartphones").build();
  }

  @Test
  @DisplayName("Given categories exist, when find all, then returns paged categories")
  void givenCategoriesExist_whenFindAll_thenReturnsPagedCategories() {
    Pageable pageable = PageRequest.of(0, 10);
    when(categoryRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(category), pageable, 1));

    PagedResponse<CategoryResponse> response = categoryService.findAll(pageable);

    assertThat(response.getItems())
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo((byte) 1);
          assertThat(item.getTitle()).isEqualTo("Smartphones");
          assertThat(item.getSlug()).isEqualTo("smartphones");
          assertThat(item.getDeletedAt()).isNull();
        });
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given no categories, when find all, then returns empty page")
  void givenNoCategories_whenFindAll_thenReturnsEmptyPage() {
    Pageable pageable = PageRequest.of(0, 10);
    when(categoryRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    PagedResponse<CategoryResponse> response = categoryService.findAll(pageable);

    assertThat(response.getItems()).isEmpty();
    assertThat(response.getPagination().getTotalItems()).isZero();
    assertThat(response.getPagination().getTotalPages()).isZero();
  }

  @Test
  @DisplayName("Given category exists, when find by id, then returns category")
  void givenCategoryExists_whenFindById_thenReturnsCategory() {
    when(categoryRepository.findById(anyByte())).thenReturn(Optional.of(category));

    CategoryResponse response = categoryService.findById((byte) 1);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo((byte) 1);
    assertThat(response.getTitle()).isEqualTo("Smartphones");
    assertThat(response.getSlug()).isEqualTo("smartphones");
  }

  @Test
  @DisplayName("Given category does not exist, when find by id, then throws not found exception")
  void givenCategoryDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(categoryRepository.findById(anyByte())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.findById((byte) 99))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.CATEGORY_NOT_FOUND_EXCEPTION);
  }
}
