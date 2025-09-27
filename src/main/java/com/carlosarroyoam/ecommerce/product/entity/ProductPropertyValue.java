package com.carlosarroyoam.ecommerce.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_property_values", uniqueConstraints = {
    @UniqueConstraint(name = "product_property_values_idx", columnNames = { "product_id",
        "property_id" }) })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductPropertyValue {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 45, nullable = false)
  private String value;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "product_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
  private Product product;

  @Column(name = "property_id", nullable = false)
  private Long propertyId;

  @ManyToOne
  @JoinColumn(name = "property_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
  private Property property;
}
