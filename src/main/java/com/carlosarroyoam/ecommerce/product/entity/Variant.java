package com.carlosarroyoam.ecommerce.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "variants")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Variant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sku", length = 64, unique = true, nullable = false)
  private String sku;

  @Column(name = "price", nullable = false)
  private BigDecimal price;

  @Column(name = "compared_at_price", nullable = false)
  private BigDecimal comparedAtPrice;

  @Column(name = "cost_per_item", nullable = false)
  private BigDecimal costPerItem;

  @Column(name = "quantity_on_stock", nullable = false)
  private Integer quantityOnStock;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
  private Product product;

  @Builder.Default
  @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY)
  private List<VariantAttributeValue> attributes = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY)
  private List<VariantImage> images = new ArrayList<>();
}
