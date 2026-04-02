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
@Table(name = "variant_attribute_values", uniqueConstraints = {
    @UniqueConstraint(name = "variant_attribute_values_idx", columnNames = { "variant_id",
        "attribute_id" }) })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariantAttributeValue {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "value", length = 45, nullable = false)
  private String value;

  @ManyToOne
  @JoinColumn(name = "variant_id", referencedColumnName = "id", nullable = false)
  private Variant variant;

  @ManyToOne
  @JoinColumn(name = "attribute_id", referencedColumnName = "id", nullable = false)
  private Attribute attribute;
}
