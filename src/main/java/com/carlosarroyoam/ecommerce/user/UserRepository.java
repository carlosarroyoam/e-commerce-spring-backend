package com.carlosarroyoam.ecommerce.user;

import com.carlosarroyoam.ecommerce.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos para la entidad {@link User}. */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  /**
   * Busca un usuario STAFF por su email.
   *
   * @param email el email del usuario
   * @return el usuario encontrado, o {@link Optional#empty()} si no existe ninguno con ese email
   */
  Optional<User> findByEmail(String email);
}
