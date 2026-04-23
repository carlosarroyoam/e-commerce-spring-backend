package com.carlosarroyoam.ecommerce.customer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_addresses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerAddress {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "street_name", length = 64, nullable = false)
  private String streetName;

  @Column(name = "street_number", length = 5, nullable = false)
  private String streetNumber;

  @Column(name = "apartament_number", length = 5)
  private String apartamentNumber;

  @Column(name = "sublocality", length = 45, nullable = false)
  private String sublocality;

  @Column(name = "locality", length = 45, nullable = false)
  private String locality;

  @Column(name = "state", length = 45, nullable = false)
  private String state;

  @Column(name = "postal_code", length = 5, nullable = false)
  private String postalCode;

  @Column(name = "country", length = 2, columnDefinition = "CHAR(2)")
  private String country;

  @Column(name = "phone_number", length = 10, nullable = false)
  private String phoneNumber;

  @Column(name = "is_default", columnDefinition = "TINYINT", nullable = false)
  private Boolean isDefault;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
  private Customer customer;
}
