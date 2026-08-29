package com.carlosarroyoam.ecommerce.user;

import com.carlosarroyoam.ecommerce.auth.principal.AuthPrincipal;
import com.carlosarroyoam.ecommerce.auth.principal.PrincipalType;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserSpecs;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints REST de administración de usuarios STAFF bajo {@code /users}, restringidos
 * al rol {@code ADMIN}.
 */
@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  public UserController(final UserService userService) {
    this.userService = userService;
  }

  /**
   * Lista los usuarios STAFF de forma paginada y filtrable.
   *
   * @param userSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de usuarios y el estado 200 OK
   */
  @GetMapping(produces = "application/json")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PagedResponse<UserResponse>> findAll(
      @Valid @ModelAttribute UserSpecs userSpecs,
      @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
    PagedResponse<UserResponse> users = userService.findAll(userSpecs, pageable);
    return ResponseEntity.ok(users);
  }

  /**
   * Obtiene un usuario STAFF por su id.
   *
   * @param userId el id del usuario
   * @return el usuario encontrado y el estado 200 OK
   */
  @GetMapping(value = "/{userId}", produces = "application/json")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> findById(@PathVariable Long userId) {
    UserResponse userById = userService.findById(userId);
    return ResponseEntity.ok(userById);
  }

  /**
   * Elimina (baja lógica) un usuario STAFF por su id. Un usuario STAFF no puede eliminarse a sí
   * mismo.
   *
   * @param userId el id del usuario a eliminar
   * @param principal el principal autenticado que realiza la petición
   * @return estado 204 No Content
   */
  @DeleteMapping("/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteById(
      @PathVariable Long userId, @AuthenticationPrincipal AuthPrincipal principal) {
    Long currentUserId =
        principal.getPrincipalType() == PrincipalType.STAFF ? principal.getId() : null;

    userService.deleteById(userId, currentUserId);
    return ResponseEntity.noContent().build();
  }
}
