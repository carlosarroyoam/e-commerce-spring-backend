package com.carlosarroyoam.ecommerce.admin.entity;

import com.carlosarroyoam.ecommerce.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins", uniqueConstraints = {
    @UniqueConstraint(name = "uk_admins_user_id", columnNames = "user_id") })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Admin {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "is_super", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
  private Boolean isSuper;

  @JsonIgnore
  @OneToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;
}