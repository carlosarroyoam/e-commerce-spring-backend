package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.category.entity.Category_;
import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.product.dto.ProductResponse;
import com.carlosarroyoam.ecommerce.product.dto.ProductResponse.ProductResponseMapper;
import com.carlosarroyoam.ecommerce.product.dto.ProductSpecs;
import com.carlosarroyoam.ecommerce.product.entity.Product;
import com.carlosarroyoam.ecommerce.product.entity.Product_;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lógica de negocio para consultar productos ({@link Product}). */
@Service
public class ProductService {
  private static final Logger log = LoggerFactory.getLogger(ProductService.class);
  private final ProductRepository productRepository;

  public ProductService(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  /**
   * Obtiene una página de productos que cumplen los filtros indicados.
   *
   * @param productSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de {@link ProductResponse} resultante
   */
  @Transactional(readOnly = true)
  public PagedResponse<ProductResponse> findAll(ProductSpecs productSpecs, Pageable pageable) {
    Specification<Product> spec =
        SpecificationBuilder.<Product>builder()
            .likeIfPresent(root -> root.get(Product_.title), productSpecs.getTitle())
            .likeIfPresent(root -> root.get(Product_.slug), productSpecs.getSlug())
            .equalsIfPresent(root -> root.get(Product_.isFeatured), productSpecs.getIsFeatured())
            .equalsIfPresent(root -> root.get(Product_.isActive), productSpecs.getIsActive())
            .equalsIfPresent(
                root -> root.join(Product_.category, JoinType.INNER).get(Category_.id),
                productSpecs.getCategoryId())
            .betweenDatesIfPresent(
                root -> root.get(Product_.createdAt),
                productSpecs.getStartDate(),
                productSpecs.getEndDate())
            .build();

    Page<Product> products = productRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        products.map(ProductResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un producto por su id.
   *
   * @param productId el id del producto
   * @return el {@link ProductResponse} correspondiente
   * @throws ResourceNotFoundException con 404 si no existe un producto con ese id
   */
  @Transactional(readOnly = true)
  public ProductResponse findById(Long productId) {
    Product productById = findProductByIdOrFail(productId);
    return ProductResponseMapper.INSTANCE.toDto(productById);
  }

  /**
   * Busca un producto por su id o lanza una excepción si no existe.
   *
   * @param productId el id del producto
   * @return el {@link Product} encontrado
   * @throws ResourceNotFoundException con 404 si no existe un producto con ese id
   */
  private Product findProductByIdOrFail(Long productId) {
    return productRepository
        .findById(productId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
              return new ResourceNotFoundException(AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
            });
  }
}
