package com.carlosarroyoam.ecommerce.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que representa el tipo de un movimiento de inventario (p. ej. entrada o salida)
 * persistido en la tabla {@code movement_types}.
 */
@Entity
@Table(name = "movement_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovementType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Byte id;

  @Column(name = "title", length = 64, nullable = false)
  private String title;
}
