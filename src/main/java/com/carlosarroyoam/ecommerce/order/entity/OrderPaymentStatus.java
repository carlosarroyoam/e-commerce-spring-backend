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
@Table(name = "order_payment_statuses", uniqueConstraints = {
    @UniqueConstraint(name = "uk_order_payment_statuses_title", columnNames = "title") })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPaymentStatus {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Byte id;

  @Column(name = "title", length = 32, nullable = false)
  private String title;
}