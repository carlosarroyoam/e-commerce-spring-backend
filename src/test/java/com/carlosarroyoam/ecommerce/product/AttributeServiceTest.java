package com.carlosarroyoam.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.product.dto.AttributeResponse;
import com.carlosarroyoam.ecommerce.product.entity.Attribute;
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
class AttributeServiceTest {
  @Mock private AttributeRepository attributeRepository;
  @InjectMocks private AttributeService attributeService;

  private Attribute attribute;

  @BeforeEach
  void setUp() {
    attribute = Attribute.builder().id(1L).title("Color").build();
  }

  @Test
  @DisplayName("Given attributes exist, when find all, then returns paged attributes")
  void givenAttributesExist_whenFindAll_thenReturnsPagedAttributes() {
    Pageable pageable = PageRequest.of(0, 10);
    when(attributeRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(attribute), pageable, 1));

    PagedResponse<AttributeResponse> response = attributeService.findAll(pageable);

    assertThat(response.getItems())
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo(1L);
          assertThat(item.getTitle()).isEqualTo("Color");
          assertThat(item.getDeletedAt()).isNull();
        });
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given no attributes, when find all, then returns empty page")
  void givenNoAttributes_whenFindAll_thenReturnsEmptyPage() {
    Pageable pageable = PageRequest.of(0, 10);
    when(attributeRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    PagedResponse<AttributeResponse> response = attributeService.findAll(pageable);

    assertThat(response.getItems()).isEmpty();
    assertThat(response.getPagination().getTotalItems()).isZero();
    assertThat(response.getPagination().getTotalPages()).isZero();
  }

  @Test
  @DisplayName("Given attribute exists, when find by id, then returns attribute")
  void givenAttributeExists_whenFindById_thenReturnsAttribute() {
    when(attributeRepository.findById(anyLong())).thenReturn(Optional.of(attribute));

    AttributeResponse response = attributeService.findById(1L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getTitle()).isEqualTo("Color");
  }

  @Test
  @DisplayName("Given attribute does not exist, when find by id, then throws not found exception")
  void givenAttributeDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(attributeRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> attributeService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.ATTRIBUTE_NOT_FOUND_EXCEPTION);
  }
}
