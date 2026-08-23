package com.carlosarroyoam.ecommerce.user;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse.UserResponseMapper;
import com.carlosarroyoam.ecommerce.user.dto.UserSpecs;
import com.carlosarroyoam.ecommerce.user.entity.Role_;
import com.carlosarroyoam.ecommerce.user.entity.User;
import com.carlosarroyoam.ecommerce.user.entity.UserStatus;
import com.carlosarroyoam.ecommerce.user.entity.User_;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Lógica de negocio para consultar y eliminar usuarios STAFF ({@link User}). */
@Service
public class UserService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Obtiene una página de usuarios STAFF que cumplen los filtros indicados.
   *
   * @param userSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de {@link UserResponse} resultante
   */
  @Transactional(readOnly = true)
  public PagedResponse<UserResponse> findAll(UserSpecs userSpecs, Pageable pageable) {
    Specification<User> spec =
        SpecificationBuilder.<User>builder()
            .likeIfPresent(root -> root.get(User_.firstName), userSpecs.getFirstName())
            .likeIfPresent(root -> root.get(User_.lastName), userSpecs.getLastName())
            .likeIfPresent(root -> root.get(User_.email), userSpecs.getEmail())
            .equalsIfPresent(root -> root.get(User_.status), userSpecs.getStatus())
            .betweenDatesIfPresent(
                root -> root.get(User_.createdAt), userSpecs.getStartDate(), userSpecs.getEndDate())
            .inIfPresent(
                root -> root.join(User_.roles, JoinType.INNER).get(Role_.id),
                userSpecs.getRoleIds())
            .build();

    Page<User> users = userRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        users.map(UserResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un usuario STAFF por su id.
   *
   * @param userId el id del usuario
   * @return el {@link UserResponse} correspondiente
   * @throws ResponseStatusException con 404 si no existe un usuario con ese id
   */
  @Transactional(readOnly = true)
  public UserResponse findById(Long userId) {
    User userById = findUserByIdOrFail(userId);
    return UserResponseMapper.INSTANCE.toDto(userById);
  }

  /**
   * Marca como eliminado (baja lógica) el usuario STAFF indicado.
   *
   * @param userId el id del usuario a eliminar
   * @param currentUserId el id del principal STAFF autenticado que realiza la petición, o
   *     {@code null} si el principal autenticado es un CUSTOMER
   * @throws ResponseStatusException con 422 si {@code userId} coincide con {@code currentUserId}
   *     o si el usuario ya está eliminado; con 404 si no existe un usuario con ese id
   */
  @Transactional
  public void deleteById(Long userId, Long currentUserId) {
    if (userId.equals(currentUserId)) {
      log.warn(AppMessages.USER_CANNOT_DELETE_ITSELF_EXCEPTION);
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, AppMessages.USER_CANNOT_DELETE_ITSELF_EXCEPTION);
    }

    User userById = findUserByIdOrFail(userId);

    if (userById.getStatus() == UserStatus.DELETED) {
      log.warn(AppMessages.USER_CANNOT_BE_DELETED_EXCEPTION);
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, AppMessages.USER_CANNOT_BE_DELETED_EXCEPTION);
    }

    userById.setStatus(UserStatus.DELETED);
    userById.setDeletedAt(LocalDateTime.now());
    userById.setUpdatedAt(LocalDateTime.now());
    userRepository.save(userById);
  }

  /**
   * Busca un usuario staff por su id o lanza una excepción si no existe.
   *
   * @param userId el id del usuario
   * @return el {@link User} encontrado
   * @throws ResponseStatusException con 404 si no existe un usuario con ese id
   */
  private User findUserByIdOrFail(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND_EXCEPTION);
            });
  }
}
