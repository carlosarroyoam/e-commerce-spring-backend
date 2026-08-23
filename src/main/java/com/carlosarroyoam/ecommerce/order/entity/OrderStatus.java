package com.carlosarroyoam.ecommerce.order.entity;

/** Estados posibles de una orden. */
public enum OrderStatus {
  PENDING,
  CONFIRMED,
  PROCESSING,
  SHIPPED,
  DELIVERED,
  CANCELLED,
  REFUNDED
}
