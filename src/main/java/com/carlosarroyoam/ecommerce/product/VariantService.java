package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse.VariantResponseMapper;
import com.carlosarroyoam.ecommerce.product.entity.Variant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lógica de negocio para consultar variantes de producto ({@link Variant}). */
@Service
public class VariantService {
  private static final Logger log = LoggerFactory.getLogger(VariantService.class);
  private final ProductRepository productRepository;
  private final VariantRepository variantRepository;

  public VariantService(
      final ProductRepository productRepository, final VariantRepository variantRepository) {
    this.productRepository = productRepository;
    this.variantRepository = variantRepository;
  }

  /**
   * Lista todas las variantes de un producto.
   *
   * @param productId el id del producto
   * @return la lista de {@link VariantResponse}
   * @throws ResourceNotFoundException con 404 si no existe un producto con ese id
   */
  @Transactional(readOnly = true)
  public List<VariantResponse> findAllByProductId(Long productId) {
    validateProductExists(productId);

    return VariantResponseMapper.INSTANCE.toDtos(variantRepository.findAllByProductId(productId));
  }

  /**
   * Busca una variante de un producto por su id.
   *
   * @param productId el id del producto
   * @param variantId el id de la variante
   * @return el {@link VariantResponse} correspondiente
   * @throws ResourceNotFoundException con 404 si no existe el producto o la variante
   */
  @Transactional(readOnly = true)
  public VariantResponse findById(Long productId, Long variantId) {
    validateProductExists(productId);

    Variant variantById =
        variantRepository
            .findByIdAndProductId(variantId, productId)
            .orElseThrow(
                () -> {
                  log.warn(AppMessages.PRODUCT_VARIANT_NOT_FOUND_EXCEPTION);
                  return new ResourceNotFoundException(
                      AppMessages.PRODUCT_VARIANT_NOT_FOUND_EXCEPTION);
                });

    return VariantResponseMapper.INSTANCE.toDto(variantById);
  }

  /**
   * Verifica que exista un producto con el id indicado.
   *
   * @param productId el id del producto
   * @throws ResourceNotFoundException con 404 si no existe un producto con ese id
   */
  private void validateProductExists(Long productId) {
    if (!productRepository.existsById(productId)) {
      log.warn(AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
      throw new ResourceNotFoundException(AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
    }
  }
}
