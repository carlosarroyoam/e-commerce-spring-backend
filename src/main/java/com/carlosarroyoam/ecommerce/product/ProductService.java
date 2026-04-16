package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.category.entity.Category_;
import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {
  private static final Logger log = LoggerFactory.getLogger(ProductService.class);
  private final ProductRepository productRepository;

  public ProductService(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public PagedResponse<ProductResponse> findAll(ProductSpecs productSpecs, Pageable pageable) {
    Specification<Product> spec = SpecificationBuilder.<Product>builder()
        .likeIfPresent(root -> root.get(Product_.title), productSpecs.getTitle())
        .likeIfPresent(root -> root.get(Product_.slug), productSpecs.getSlug())
        .equalsIfPresent(root -> root.get(Product_.isFeatured), productSpecs.getIsFeatured())
        .equalsIfPresent(root -> root.get(Product_.isActive), productSpecs.getIsActive())
        .equalsIfPresent(root -> root.join(Product_.category, JoinType.INNER).get(Category_.id),
            productSpecs.getCategoryId())
        .betweenDatesIfPresent(root -> root.get(Product_.createdAt), productSpecs.getStartDate(),
            productSpecs.getEndDate())
        .build();

    Page<Product> products = productRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(products.map(ProductResponseMapper.INSTANCE::toDto));
  }

  public ProductResponse findById(Long productId) {
    Product productById = findProductByIdOrFail(productId);
    return ProductResponseMapper.INSTANCE.toDto(productById);
  }

  private Product findProductByIdOrFail(Long productId) {
    return productRepository.findById(productId).orElseThrow(() -> {
      log.warn(AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
    });
  }
}
