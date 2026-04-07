package com.carlosarroyoam.ecommerce.product;

import com.carlosarroyoam.ecommerce.category.entity.Category_;
import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponseDto;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponseDto.PagedResponseDtoMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.product.dto.ProductDto;
import com.carlosarroyoam.ecommerce.product.dto.ProductDto.ProductDtoMapper;
import com.carlosarroyoam.ecommerce.product.dto.ProductSpecsDto;
import com.carlosarroyoam.ecommerce.product.entity.Product;
import com.carlosarroyoam.ecommerce.product.entity.Product_;
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

  public PagedResponseDto<ProductDto> findAll(ProductSpecsDto productSpecs, Pageable pageable) {
    Specification<Product> spec = SpecificationBuilder.<Product>builder()
        .likeIfPresent(root -> root.get(Product_.title), productSpecs.getTitle())
        .likeIfPresent(root -> root.get(Product_.slug), productSpecs.getSlug())
        .equalsIfPresent(root -> root.get(Product_.isFeatured), productSpecs.getIsFeatured())
        .equalsIfPresent(root -> root.get(Product_.isActive), productSpecs.getIsActive())
        .equalsIfPresent(root -> root.join(Product_.category).get(Category_.id),
            productSpecs.getCategoryId())
        .betweenDatesIfPresent(root -> root.get(Product_.createdAt), productSpecs.getStartDate(),
            productSpecs.getEndDate())
        .build();

    Page<Product> products = productRepository.findAll(spec, pageable);

    return PagedResponseDtoMapper.INSTANCE
        .toPagedResponseDto(products.map(ProductDtoMapper.INSTANCE::toDto));
  }

  public ProductDto findById(Long productId) {
    Product productById = findProductEntityById(productId);
    return ProductDtoMapper.INSTANCE.toDto(productById);
  }

  private Product findProductEntityById(Long productId) {
    return productRepository.findById(productId).orElseThrow(() -> {
      log.warn(AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.PRODUCT_NOT_FOUND_EXCEPTION);
    });
  }
}
