package com.carlosarroyoam.ecommerce.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.ecommerce.core.constant.MessageKeys;
import com.carlosarroyoam.ecommerce.core.exception.BusinessException;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.core.pagination.PagedResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserSpecs;
import com.carlosarroyoam.ecommerce.user.entity.Role;
import com.carlosarroyoam.ecommerce.user.entity.User;
import com.carlosarroyoam.ecommerce.user.entity.UserStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserRepository userRepository;
  @Spy private Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);
  @InjectMocks private UserService userService;

  private User user;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now(clock);

    user =
        User.builder()
            .id(2L)
            .firstName("Cathy")
            .lastName("Guido")
            .email("cathy.guido@e-commerce.com")
            .status(UserStatus.ACTIVE)
            .roles(
                Set.of(
                    Role.builder()
                        .id((byte) 1)
                        .name("ADMIN")
                        .description("Admin user role")
                        .build()))
            .createdAt(now.minusDays(1))
            .updatedAt(now.minusDays(1))
            .build();
  }

  @Test
  @DisplayName("Given users exist, when find all, then returns paged users")
  void givenUsersExist_whenFindAll_thenReturnsPagedUsers() {
    UserSpecs userSpecs = UserSpecs.builder().firstName("Cathy").status(UserStatus.ACTIVE).build();
    Pageable pageable = PageRequest.of(0, 10);
    when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

    PagedResponse<UserResponse> response = userService.findAll(userSpecs, pageable);

    assertThat(response.getItems())
        .hasSize(1)
        .first()
        .satisfies(item -> {
          assertThat(item.getEmail()).isEqualTo("cathy.guido@e-commerce.com");
          assertThat(item.getRoles()).extracting("name").containsExactly("ADMIN");
        });
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(10);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given user exists, when find by id, then returns user")
  void givenUserExists_whenFindById_thenReturnsUser() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

    UserResponse response = userService.findById(2L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(2L);
    assertThat(response.getEmail()).isEqualTo("cathy.guido@e-commerce.com");
    assertThat(response.getRoles()).extracting("name").containsExactly("ADMIN");
  }

  @Test
  @DisplayName("Given user does not exist, when find by id, then throws not found exception")
  void givenUserDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.USER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given active user, when delete by id, then marks user as deleted")
  void givenActiveUser_whenDeleteById_thenMarksUserAsDeleted() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

    userService.deleteById(2L, 1L);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    LocalDateTime now = LocalDateTime.now(clock);
    assertThat(captor.getValue().getStatus()).isEqualTo(UserStatus.DELETED);
    assertThat(captor.getValue().getDeletedAt()).isEqualTo(now);
    assertThat(captor.getValue().getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("Given inactive user, when delete by id, then marks user as deleted")
  void givenInactiveUser_whenDeleteById_thenMarksUserAsDeleted() {
    user.setStatus(UserStatus.INACTIVE);
    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

    userService.deleteById(2L, 1L);

    verify(userRepository).save(eq(user));
    assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
  }

  @Test
  @DisplayName(
      "Given user id equals current user id, when delete by id, then throws business exception")
  void givenUserIdEqualsCurrentUserId_whenDeleteById_thenThrowsBusinessException() {
    assertThatThrownBy(() -> userService.deleteById(1L, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessage(MessageKeys.USER_CANNOT_DELETE_ITSELF_EXCEPTION);

    verify(userRepository, never()).findById(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Given user does not exist, when delete by id, then throws not found exception")
  void givenUserDoesNotExist_whenDeleteById_thenThrowsNotFoundException() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.deleteById(999L, 1L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.USER_NOT_FOUND_EXCEPTION);

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Given user already deleted, when delete by id, then throws business exception")
  void givenUserAlreadyDeleted_whenDeleteById_thenThrowsBusinessException() {
    user.setStatus(UserStatus.DELETED);
    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> userService.deleteById(2L, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessage(MessageKeys.USER_CANNOT_BE_DELETED_EXCEPTION);

    verify(userRepository, never()).save(any());
  }
}
