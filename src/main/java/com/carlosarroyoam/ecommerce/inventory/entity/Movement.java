package com.carlosarroyoam.ecommerce.inventory.entity;

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
@Table(name = "movements", uniqueConstraints = {
    @UniqueConstraint(name = "uk_movements_title", columnNames = "title") })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Movement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Byte id;

  @Column(name = "title", length = 45, nullable = false)
  private String title;

  @ManyToOne
  @JoinColumn(name = "movement_type_id", referencedColumnName = "id", nullable = false)
  private MovementType movementType;
}