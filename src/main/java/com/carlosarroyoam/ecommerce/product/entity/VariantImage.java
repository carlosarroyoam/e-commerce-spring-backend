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
@Table(name = "variant_images", uniqueConstraints = {
    @UniqueConstraint(name = "variant_images_url_idx", columnNames = "url") })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariantImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "url", length = 128, unique = true, nullable = false)
  private String url;

  @Column(name = "variant_id", nullable = false)
  private Long variantId;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "variant_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
  private Variant variant;
}
