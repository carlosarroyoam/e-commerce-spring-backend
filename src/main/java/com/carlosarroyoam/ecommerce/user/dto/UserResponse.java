package com.carlosarroyoam.ecommerce.user.dto;

import com.carlosarroyoam.ecommerce.user.dto.UserRoleResponse.UserRoleResponseMapper;
import com.carlosarroyoam.ecommerce.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private Boolean isActive;
  private UserRoleResponse userRole;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      UserRoleResponseMapper.class })
  public interface UserResponseMapper {
    UserResponseMapper INSTANCE = Mappers.getMapper(UserResponseMapper.class);

    UserResponse toDto(User entity);

    List<UserResponse> toDtos(List<User> entities);
  }
}