package com.carlosarroyoam.ecommerce.user;

import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserSpecs;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  public UserController(final UserService userService) {
    this.userService = userService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<UserResponse>> findAll(
      @Valid @ModelAttribute UserSpecs productSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<UserResponse> products = userService.findAll(productSpecs, pageable);
    return ResponseEntity.ok(products);
  }

  @GetMapping(value = "/{userId}", produces = "application/json")
  public ResponseEntity<UserResponse> findById(@PathVariable Long userId) {
    UserResponse userById = userService.findById(userId);
    return ResponseEntity.ok(userById);
  }
}
