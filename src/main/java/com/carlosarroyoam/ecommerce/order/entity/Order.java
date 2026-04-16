package com.carlosarroyoam.ecommerce.order.entity;

import com.carlosarroyoam.ecommerce.customer.entity.Customer;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
import com.carlosarroyoam.ecommerce.payment.entity.Payment;
import com.carlosarroyoam.ecommerce.refund.entity.Refund;
import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_number", length = 36, nullable = false)
  private String orderNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
  private Customer customer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shipping_address_id", referencedColumnName = "id", nullable = false)
  private CustomerAddress shippingAddress;

  @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "tax_total", nullable = false, precision = 10, scale = 2)
  private BigDecimal taxTotal;

  @Column(name = "shipping_total", nullable = false, precision = 10, scale = 2)
  private BigDecimal shippingTotal;

  @Column(name = "total", nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 32, nullable = false)
  private OrderStatus status;

  @Builder.Default
  @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
  private List<OrderItem> items = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
  private List<Payment> payments = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
  private List<Shipment> shipments = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
  private List<Refund> refunds = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private List<OrderStatusHistory> statusHistory = new ArrayList<>();

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
