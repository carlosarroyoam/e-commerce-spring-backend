package com.carlosarroyoam.ecommerce.refund.entity;

import com.carlosarroyoam.ecommerce.order.entity.Order;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refunds")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Refund {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
  private Order order;

  @Column(name = "amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(name = "reason", columnDefinition = "TEXT")
  private String reason;

  @OneToMany(mappedBy = "refund")
  private List<RefundItem> items;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}