package com.carlosarroyoam.ecommerce.product.entity;

import com.carlosarroyoam.ecommerce.category.entity.Category;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title", length = 96, nullable = false)
  private String title;

  @Column(name = "slug", length = 96, nullable = false)
  private String slug;

  @Column(name = "description")
  private String description;

  @Column(name = "is_featured", columnDefinition = "TINYINT", nullable = false)
  private Boolean isFeatured;

  @Column(name = "is_active", columnDefinition = "TINYINT", nullable = false)
  private Boolean isActive;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
  private Category category;

  @Builder.Default
  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  private List<ProductPropertyValue> properties = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  private List<Variant> variants = new ArrayList<>();

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
