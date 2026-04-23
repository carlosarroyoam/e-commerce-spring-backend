package com.carlosarroyoam.ecommerce.category.entity;

import com.carlosarroyoam.ecommerce.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Byte id;

  @Column(name = "title", length = 64, unique = true, nullable = false)
  private String title;

  @Builder.Default
  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
  private List<Product> products = new ArrayList<>();

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
