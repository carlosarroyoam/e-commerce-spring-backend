package com.carlosarroyoam.ecommerce.user;

import com.carlosarroyoam.ecommerce.core.dto.PagedResponseDto;
import com.carlosarroyoam.ecommerce.user.dto.UserDto;
import com.carlosarroyoam.ecommerce.user.dto.UserSpecsDto;
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
  public ResponseEntity<PagedResponseDto<UserDto>> findAll(
      @Valid @ModelAttribute UserSpecsDto productSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponseDto<UserDto> products = userService.findAll(productSpecs, pageable);
    return ResponseEntity.ok(products);
  }

  @GetMapping(value = "/{userId}", produces = "application/json")
  public ResponseEntity<UserDto> findById(@PathVariable Long userId) {
    UserDto userById = userService.findById(userId);
    return ResponseEntity.ok(userById);
  }
}
