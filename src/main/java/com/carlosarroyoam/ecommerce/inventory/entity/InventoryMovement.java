package com.carlosarroyoam.ecommerce.inventory.entity;

import com.carlosarroyoam.ecommerce.order.entity.Movement;
import com.carlosarroyoam.ecommerce.product.entity.Variant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_movements")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryMovement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "variant_id", referencedColumnName = "id", nullable = false)
  private Variant variant;

  @ManyToOne
  @JoinColumn(name = "movement_id", referencedColumnName = "id", nullable = false)
  private Movement movement;
}