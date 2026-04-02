package com.carlosarroyoam.ecommerce.product.entity;

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

  @ManyToOne
  @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
  private Product product;

  @ManyToOne
  @JoinColumn(name = "property_id", referencedColumnName = "id", nullable = false)
  private Property property;
}
