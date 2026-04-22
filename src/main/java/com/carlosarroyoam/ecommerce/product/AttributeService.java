package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.product.dto.AttributeResponse;
import com.carlosarroyoam.ecommerce.product.dto.AttributeResponse.AttributeResponseMapper;
import com.carlosarroyoam.ecommerce.product.entity.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttributeService {
  private static final Logger log = LoggerFactory.getLogger(AttributeService.class);
  private final AttributeRepository attributeRepository;

  public AttributeService(final AttributeRepository attributeRepository) {
    this.attributeRepository = attributeRepository;
  }

  @Transactional(readOnly = true)
  public PagedResponse<AttributeResponse> findAll(Pageable pageable) {
    Page<Attribute> attributes = attributeRepository.findAll(pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        attributes.map(AttributeResponseMapper.INSTANCE::toDto));
  }

  @Transactional(readOnly = true)
  public AttributeResponse findById(Long attributeId) {
    Attribute attributeById = findAttributeByIdOrFail(attributeId);
    return AttributeResponseMapper.INSTANCE.toDto(attributeById);
  }

  private Attribute findAttributeByIdOrFail(Long attributeId) {
    return attributeRepository
        .findById(attributeId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.ATTRIBUTE_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.ATTRIBUTE_NOT_FOUND_EXCEPTION);
            });
  }
}
