package com.carlosarroyoam.ecommerce.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attributes", uniqueConstraints = {
    @UniqueConstraint(name = "uk_attributes_title", columnNames = "title") })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Attribute {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title", length = 45, unique = true, nullable = false)
  private String title;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
