package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.entity.RefreshToken;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByFingerprint(String fingerprint);

  Optional<RefreshToken> findByFingerprintAndPrincipalType(String fingerprint,
      PrincipalType principalType);
}
