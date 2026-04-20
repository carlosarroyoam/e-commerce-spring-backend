package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse;
import com.carlosarroyoam.ecommerce.product.dto.VariantResponse.VariantResponseMapper;
import com.carlosarroyoam.ecommerce.product.entity.Variant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VariantService {
  private static final Logger log = LoggerFactory.getLogger(VariantService.class);
  private final ProductRepository productRepository;
  private final VariantRepository variantRepository;

  public VariantService(final ProductRepository productRepository,
      final VariantRepository variantRepository) {
    this.productRepository = productRepository;
    this.variantRepository = variantRepository;
  }

  @Transactional(readOnly = true)
  public List<VariantResponse> findAllByProductId(Long productId) {
    validateProductExists(productId);

    return VariantResponseMapper.INSTANCE.toDtos(variantRepository.findAllByProductId(productId));
  }

  @Transactional(readOnly = true)
  public VariantResponse findById(Long productId, Long variantId) {
    validateProductExists(productId);

    Variant variantById = variantRepository.findByIdAndProductId(variantId, productId)
        .orElseThrow(() -> {
          log.warn(AppMessages.PRODUCT_VARIANT_NOT_FOUND_EXCEPTION);
          return new ResponseStatusException(HttpStatus.NOT_FOUND,
              AppMessages.PRODUCT_VARIANT_NOT_FOUND_EXCEPTION);
        });

    return VariantResponseMapper.INSTANCE.toDto(variantById);
  }

  private void validateProductExists(Long productId) {
    if (!productRepository.existsById(productId)) {
      log.warn(AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
    }
  }
}
