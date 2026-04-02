package com.carlosarroyoam.ecommerce.order.entity;

import com.carlosarroyoam.ecommerce.customer.entity.Customer;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
import com.carlosarroyoam.ecommerce.refund.entity.Refund;
import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders", uniqueConstraints = {
    @UniqueConstraint(name = "uk_orders_order_number", columnNames = "order_number") })
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

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
  private Customer customer;

  @ManyToOne
  @JoinColumn(name = "shipping_address_id", referencedColumnName = "id", nullable = false)
  private CustomerAddress shippingAddress;

  @ManyToOne
  @JoinColumn(name = "status_id", referencedColumnName = "id", nullable = false)
  private OrderStatus status;

  @ManyToOne
  @JoinColumn(name = "payment_status_id", referencedColumnName = "id", nullable = false)
  private OrderPaymentStatus paymentStatus;

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

  @OneToMany(mappedBy = "order")
  private List<OrderItem> items;

  @OneToMany(mappedBy = "order")
  private List<OrderStatusHistory> statusHistory;

  @OneToMany(mappedBy = "order")
  private List<Shipment> shipments;

  @OneToMany(mappedBy = "order")
  private List<Refund> refunds;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}