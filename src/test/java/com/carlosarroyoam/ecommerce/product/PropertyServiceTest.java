package com.carlosarroyoam.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.product.dto.PropertyResponse;
import com.carlosarroyoam.ecommerce.product.entity.Property;
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
class PropertyServiceTest {
  @Mock private PropertyRepository propertyRepository;
  @InjectMocks private PropertyService propertyService;

  private Property property;

  @BeforeEach
  void setUp() {
    property = Property.builder().id(1L).title("Brand").build();
  }

  @Test
  @DisplayName("Given properties exist, when find all, then returns paged properties")
  void givenPropertiesExist_whenFindAll_thenReturnsPagedProperties() {
    Pageable pageable = PageRequest.of(0, 10);
    when(propertyRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(property), pageable, 1));

    PagedResponse<PropertyResponse> response = propertyService.findAll(pageable);

    assertThat(response.getItems())
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo(1L);
          assertThat(item.getTitle()).isEqualTo("Brand");
          assertThat(item.getDeletedAt()).isNull();
        });
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given no properties, when find all, then returns empty page")
  void givenNoProperties_whenFindAll_thenReturnsEmptyPage() {
    Pageable pageable = PageRequest.of(0, 10);
    when(propertyRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    PagedResponse<PropertyResponse> response = propertyService.findAll(pageable);

    assertThat(response.getItems()).isEmpty();
    assertThat(response.getPagination().getTotalItems()).isZero();
    assertThat(response.getPagination().getTotalPages()).isZero();
  }

  @Test
  @DisplayName("Given property exists, when find by id, then returns property")
  void givenPropertyExists_whenFindById_thenReturnsProperty() {
    when(propertyRepository.findById(anyLong())).thenReturn(Optional.of(property));

    PropertyResponse response = propertyService.findById(1L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getTitle()).isEqualTo("Brand");
  }

  @Test
  @DisplayName("Given property does not exist, when find by id, then throws not found exception")
  void givenPropertyDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(propertyRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> propertyService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.PROPERTY_NOT_FOUND_EXCEPTION);
  }
}
