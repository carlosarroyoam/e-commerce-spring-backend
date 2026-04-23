package com.carlosarroyoam.ecommerce.auth.entity;

import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reset_password")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPassword {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "token_hash", length = 254, nullable = false)
  private String tokenHash;

  @Column(name = "principal_id", nullable = false)
  private Long principalId;

  @Enumerated(EnumType.STRING)
  @Column(name = "principal_type", length = 32, nullable = false)
  private PrincipalType principalType;

  @Column(name = "expires_on")
  private LocalDateTime expiresOn;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
