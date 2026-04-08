package com.carlosarroyoam.ecommerce.shipment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carriers")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Carrier {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Byte id;

  @Column(name = "name", length = 45, nullable = false)
  private String name;

  @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT")
  private Boolean isActive;
}