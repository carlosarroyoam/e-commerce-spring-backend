package com.carlosarroyoam.ecommerce.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movements")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Movement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Byte id;

  @Column(name = "title", length = 64, nullable = false)
  private String title;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "movement_type_id", referencedColumnName = "id", nullable = false)
  private MovementType movementType;
}
