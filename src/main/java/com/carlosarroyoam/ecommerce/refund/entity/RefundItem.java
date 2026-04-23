package com.carlosarroyoam.ecommerce.refund.entity;

import com.carlosarroyoam.ecommerce.order.entity.OrderItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refund_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "refund_id", referencedColumnName = "id", nullable = false)
  private Refund refund;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_item_id", referencedColumnName = "id", nullable = false)
  private OrderItem orderItem;
}
