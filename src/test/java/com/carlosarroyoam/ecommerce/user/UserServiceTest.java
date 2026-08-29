package com.carlosarroyoam.ecommerce.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.exception.BusinessException;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserSpecs;
import com.carlosarroyoam.ecommerce.user.entity.Role;
import com.carlosarroyoam.ecommerce.user.entity.User;
import com.carlosarroyoam.ecommerce.user.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  private static User activeUser(Long id) {
    return User.builder()
        .id(id)
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martinez")
        .email("carlos.arroyo@e-commerce.com")
        .status(UserStatus.ACTIVE)
        .roles(
            Set.of(
                Role.builder().id((byte) 1).name("ADMIN").description("Admin user role").build()))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  @Test
  @DisplayName("Given a page of users, when find all, then returns paged response")
  void givenPageOfUsers_whenFindAll_thenMapsItToPagedResponse() {
    UserSpecs userSpecs = UserSpecs.builder().firstName("Carlos").status(UserStatus.ACTIVE).build();
    Pageable pageable = PageRequest.of(0, 10);
    when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable)))
        .thenReturn(new PageImpl<>(List.of(activeUser(1L)), pageable, 1));

    PagedResponse<UserResponse> result = userService.findAll(userSpecs, pageable);

    assertThat(result.getItems()).hasSize(1);
    assertThat(result.getItems().get(0).getEmail()).isEqualTo("carlos.arroyo@e-commerce.com");
    assertThat(result.getItems().get(0).getRoles()).extracting("name").containsExactly("ADMIN");
    assertThat(result.getPagination().getTotalItems()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given an existing user id, when find by id, then returns the user")
  void givenExistingUserId_whenFindById_thenReturnsMappedUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser(1L)));

    UserResponse result = userService.findById(1L);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getEmail()).isEqualTo("carlos.arroyo@e-commerce.com");
    assertThat(result.getRoles()).extracting("name").containsExactly("ADMIN");
  }

  @Test
  @DisplayName("Given a non existing user id, when find by id, then throws not found")
  void givenNonExistingUserId_whenFindById_thenThrowsNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(AppMessages.USER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName(
      "Given the user id equals the current user id, when delete by id, then throws cannot delete itself")
  void givenUserIdEqualsCurrentUserId_whenDeleteById_thenThrowsCannotDeleteItself() {
    assertThatThrownBy(() -> userService.deleteById(1L, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessage(AppMessages.USER_CANNOT_DELETE_ITSELF_EXCEPTION);

    verify(userRepository, never()).findById(any());
  }

  @Test
  @DisplayName("Given a non existing user id, when delete by id, then throws not found")
  void givenNonExistingUserId_whenDeleteById_thenThrowsNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.deleteById(999L, 1L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(AppMessages.USER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given an already deleted user, when delete by id, then throws cannot be deleted")
  void givenAlreadyDeletedUser_whenDeleteById_thenThrowsCannotBeDeleted() {
    User deletedUser = activeUser(6L);
    deletedUser.setStatus(UserStatus.DELETED);
    when(userRepository.findById(6L)).thenReturn(Optional.of(deletedUser));

    assertThatThrownBy(() -> userService.deleteById(6L, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessage(AppMessages.USER_CANNOT_BE_DELETED_EXCEPTION);

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Given an inactive user, when delete by id, then marks it as deleted")
  void givenInactiveUser_whenDeleteById_thenMarksAsDeleted() {
    User inactiveUser = activeUser(4L);
    inactiveUser.setStatus(UserStatus.INACTIVE);
    when(userRepository.findById(4L)).thenReturn(Optional.of(inactiveUser));

    userService.deleteById(4L, 1L);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getStatus()).isEqualTo(UserStatus.DELETED);
    assertThat(captor.getValue().getDeletedAt()).isNotNull();
    assertThat(captor.getValue().getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Given an active user, when delete by id, then marks it as deleted")
  void givenActiveUser_whenDeleteById_thenMarksAsDeleted() {
    User user = activeUser(2L);
    when(userRepository.findById(2L)).thenReturn(Optional.of(user));

    userService.deleteById(2L, 1L);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getStatus()).isEqualTo(UserStatus.DELETED);
  }
}
