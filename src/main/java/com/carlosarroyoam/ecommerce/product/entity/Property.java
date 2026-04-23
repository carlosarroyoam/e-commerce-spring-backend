package com.carlosarroyoam.ecommerce.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "properties")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Property {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title", length = 64, unique = true, nullable = false)
  private String title;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
