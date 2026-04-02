package com.carlosarroyoam.ecommerce.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refund_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "refund_id", referencedColumnName = "id", nullable = false)
  private Refund refund;

  @ManyToOne
  @JoinColumn(name = "order_item_id", referencedColumnName = "id", nullable = false)
  private OrderItem orderItem;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;
}