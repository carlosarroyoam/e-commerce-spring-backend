package com.carlosarroyoam.ecommerce.user;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse.UserResponseMapper;
import com.carlosarroyoam.ecommerce.user.dto.UserSpecs;
import com.carlosarroyoam.ecommerce.user.entity.User;
import com.carlosarroyoam.ecommerce.user.entity.UserRole_;
import com.carlosarroyoam.ecommerce.user.entity.User_;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;

  public UserService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public PagedResponse<UserResponse> findAll(UserSpecs userSpecs, Pageable pageable) {
    Specification<User> spec = SpecificationBuilder.<User>builder()
        .likeIfPresent(root -> root.get(User_.firstName), userSpecs.getFirstName())
        .likeIfPresent(root -> root.get(User_.lastName), userSpecs.getLastName())
        .likeIfPresent(root -> root.get(User_.email), userSpecs.getEmail())
        .equalsIfPresent(root -> root.get(User_.isActive), userSpecs.getIsActive())
        .betweenDatesIfPresent(root -> root.get(User_.createdAt), userSpecs.getStartDate(),
            userSpecs.getEndDate())
        .equalsIfPresent(root -> root.join(User_.userRole).get(UserRole_.id),
            userSpecs.getUserRoleId())
        .build();

    Page<User> users = userRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(users.map(UserResponseMapper.INSTANCE::toDto));
  }

  public UserResponse findById(Long userId) {
    User userById = findUserEntityById(userId);
    return UserResponseMapper.INSTANCE.toDto(userById);
  }

  private User findUserEntityById(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> {
      log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.USER_NOT_FOUND_EXCEPTION);
    });
  }
}
