package com.carlosarroyoam.ecommerce.auth;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.user.UserRepository;
import com.carlosarroyoam.ecommerce.user.entity.Role;
import com.carlosarroyoam.ecommerce.user.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga usuarios staff desde {@link UserRepository} y los expone como {@link AuthPrincipal} para
 * la autenticación de Spring Security.
 */
@Service("staffDetailsService")
public class StaffDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public StaffDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Busca un usuario staff por su email.
   *
   * @param email el email del usuario
   * @return el {@link AuthPrincipal} correspondiente
   * @throws UsernameNotFoundException si no existe un usuario con ese email
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository
        .findByEmail(email)
        .map(this::mapStaff)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
  }

  /**
   * Busca un usuario staff por su id.
   *
   * @param userId el id del usuario
   * @return el {@link AuthPrincipal} correspondiente
   * @throws UsernameNotFoundException si no existe un usuario con ese id
   */
  @Transactional(readOnly = true)
  public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
    return userRepository
        .findById(userId)
        .map(this::mapStaff)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
  }

  /**
   * Convierte una entidad {@link User} en un {@link AuthPrincipal} de tipo STAFF, incluyendo sus
   * roles.
   *
   * @param user la entidad de usuario staff a convertir
   * @return el {@link AuthPrincipal} resultante
   */
  private AuthPrincipal mapStaff(User user) {
    Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

    return AuthPrincipal.builder()
        .id(user.getId())
        .fullName(user.getFirstName() + " " + user.getLastName())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .passwordHash(user.getPasswordHash())
        .status(user.getStatus().toString())
        .principalType(PrincipalType.STAFF)
        .roles(roles)
        .build();
  }
}
