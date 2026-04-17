package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.product.dto.PropertyResponse;
import com.carlosarroyoam.ecommerce.product.dto.PropertyResponse.PropertyResponseMapper;
import com.carlosarroyoam.ecommerce.product.entity.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PropertyService {
  private static final Logger log = LoggerFactory.getLogger(PropertyService.class);
  private final PropertyRepository propertyRepository;

  public PropertyService(final PropertyRepository propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  @Transactional(readOnly = true)
  public PagedResponse<PropertyResponse> findAll(Pageable pageable) {
    Page<Property> properties = propertyRepository.findAll(pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(properties.map(PropertyResponseMapper.INSTANCE::toDto));
  }

  @Transactional(readOnly = true)
  public PropertyResponse findById(Long propertyId) {
    Property propertyById = findPropertyByIdOrFail(propertyId);
    return PropertyResponseMapper.INSTANCE.toDto(propertyById);
  }

  private Property findPropertyByIdOrFail(Long propertyId) {
    return propertyRepository.findById(propertyId).orElseThrow(() -> {
      log.warn(AppMessages.PROPERTY_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.PROPERTY_NOT_FOUND_EXCEPTION);
    });
  }
}
