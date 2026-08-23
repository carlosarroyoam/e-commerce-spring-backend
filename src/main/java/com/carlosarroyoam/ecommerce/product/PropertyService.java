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

/** Lógica de negocio para consultar propiedades de producto ({@link Property}). */
@Service
public class PropertyService {
  private static final Logger log = LoggerFactory.getLogger(PropertyService.class);
  private final PropertyRepository propertyRepository;

  public PropertyService(final PropertyRepository propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  /**
   * Obtiene una página de propiedades.
   *
   * @param pageable la paginación y el orden a aplicar
   * @return la página de {@link PropertyResponse} resultante
   */
  @Transactional(readOnly = true)
  public PagedResponse<PropertyResponse> findAll(Pageable pageable) {
    Page<Property> properties = propertyRepository.findAll(pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        properties.map(PropertyResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca una propiedad por su id.
   *
   * @param propertyId el id de la propiedad
   * @return el {@link PropertyResponse} correspondiente
   * @throws ResponseStatusException con 404 si no existe una propiedad con ese id
   */
  @Transactional(readOnly = true)
  public PropertyResponse findById(Long propertyId) {
    Property propertyById = findPropertyByIdOrFail(propertyId);
    return PropertyResponseMapper.INSTANCE.toDto(propertyById);
  }

  /**
   * Busca una propiedad por su id o lanza una excepción si no existe.
   *
   * @param propertyId el id de la propiedad
   * @return la {@link Property} encontrada
   * @throws ResponseStatusException con 404 si no existe una propiedad con ese id
   */
  private Property findPropertyByIdOrFail(Long propertyId) {
    return propertyRepository
        .findById(propertyId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.PROPERTY_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.PROPERTY_NOT_FOUND_EXCEPTION);
            });
  }
}
