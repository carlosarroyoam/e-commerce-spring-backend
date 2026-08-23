package com.carlosarroyoam.ecommerce.payment.entity;

/** Estados posibles de un pago. */
public enum PaymentStatus {
  PENDING,
  COMPLETED,
  FAILED,
  CANCELLED,
  REFUNDED
}
