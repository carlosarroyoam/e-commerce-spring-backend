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
import com.carlosarroyoam.ecommerce.user.entity.User_;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public PagedResponse<UserResponse> findAll(UserSpecs userSpecs, Pageable pageable) {
    Specification<User> spec = SpecificationBuilder.<User>builder()
        .likeIfPresent(root -> root.get(User_.firstName), userSpecs.getFirstName())
        .likeIfPresent(root -> root.get(User_.lastName), userSpecs.getLastName())
        .likeIfPresent(root -> root.get(User_.email), userSpecs.getEmail())
        .equalsIfPresent(root -> root.get(User_.status), userSpecs.getStatus())
        .betweenDatesIfPresent(root -> root.get(User_.createdAt), userSpecs.getStartDate(),
            userSpecs.getEndDate())
        .inIfPresent(root -> root.join(User_.roles, JoinType.INNER).get(Role_.id),
            userSpecs.getRoleIds())
        .build();

    Page<User> users = userRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(users.map(UserResponseMapper.INSTANCE::toDto));
  }

  @Transactional(readOnly = true)
  public UserResponse findById(Long userId) {
    User userById = findUserByIdOrFail(userId);
    return UserResponseMapper.INSTANCE.toDto(userById);
  }

  private User findUserByIdOrFail(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> {
      log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.USER_NOT_FOUND_EXCEPTION);
    });
  }
}
