package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.product.dto.AttributeResponse;
import com.carlosarroyoam.ecommerce.product.dto.AttributeResponse.AttributeResponseMapper;
import com.carlosarroyoam.ecommerce.product.entity.Attribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lógica de negocio para consultar atributos de producto ({@link Attribute}). */
@Service
public class AttributeService {
  private final AttributeRepository attributeRepository;

  public AttributeService(final AttributeRepository attributeRepository) {
    this.attributeRepository = attributeRepository;
  }

  /**
   * Obtiene una página de atributos.
   *
   * @param pageable la paginación y el orden a aplicar
   * @return la página de {@link AttributeResponse} resultante
   */
  @Transactional(readOnly = true)
  public PagedResponse<AttributeResponse> findAll(Pageable pageable) {
    Page<Attribute> attributes = attributeRepository.findAll(pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        attributes.map(AttributeResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un atributo por su id.
   *
   * @param attributeId el id del atributo
   * @return el {@link AttributeResponse} correspondiente
   * @throws ResourceNotFoundException con 404 si no existe un atributo con ese id
   */
  @Transactional(readOnly = true)
  public AttributeResponse findById(Long attributeId) {
    Attribute attributeById = findAttributeByIdOrFail(attributeId);
    return AttributeResponseMapper.INSTANCE.toDto(attributeById);
  }

  /**
   * Busca un atributo por su id o lanza una excepción si no existe.
   *
   * @param attributeId el id del atributo
   * @return el {@link Attribute} encontrado
   * @throws ResourceNotFoundException con 404 si no existe un atributo con ese id
   */
  private Attribute findAttributeByIdOrFail(Long attributeId) {
    return attributeRepository
        .findById(attributeId)
        .orElseThrow(
            () -> new ResourceNotFoundException(AppMessages.ATTRIBUTE_NOT_FOUND_EXCEPTION));
  }
}
