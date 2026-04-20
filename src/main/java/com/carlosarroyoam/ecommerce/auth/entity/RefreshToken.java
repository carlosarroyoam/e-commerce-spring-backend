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
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "token", length = 254, nullable = false)
  private String token;

  @Column(name = "fingerprint", length = 36, nullable = false)
  private String fingerprint;

  @Column(name = "principal_id", nullable = false)
  private Long principalId;

  @Enumerated(EnumType.STRING)
  @Column(name = "principal_type", length = 32, nullable = false)
  private PrincipalType principalType;

  @Column(name = "expires_on", nullable = false)
  private LocalDateTime expiresOn;

  @Column(name = "last_used_at")
  private LocalDateTime lastUsedAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}