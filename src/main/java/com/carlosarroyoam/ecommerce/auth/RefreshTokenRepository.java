package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.entity.RefreshToken;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos para la entidad {@link RefreshToken}. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  /**
   * Busca el refresh token existente para un dispositivo y principal específicos.
   *
   * @param deviceId el identificador del dispositivo
   * @param principalType el tipo de principal (STAFF o CUSTOMER)
   * @param principalId el id del principal
   * @return el refresh token encontrado, o {@link Optional#empty()} si no existe ninguno para esa
   *     combinación
   */
  Optional<RefreshToken> findByDeviceIdAndPrincipalTypeAndPrincipalId(
      String deviceId, PrincipalType principalType, Long principalId);
}
