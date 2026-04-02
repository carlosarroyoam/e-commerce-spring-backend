package com.carlosarroyoam.ecommerce.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carriers", uniqueConstraints = {
    @UniqueConstraint(name = "uk_carriers_name", columnNames = "name") })
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

  @Column(name = "active", nullable = false, columnDefinition = "BIT")
  private Boolean active;
}