package com.carlosarroyoam.ecommerce.user.dto;

import com.carlosarroyoam.ecommerce.user.entity.Role;
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
public class UserRoleResponse {
  private Byte id;
  private String type;
  private String description;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface UserRoleResponseMapper {
    UserRoleResponseMapper INSTANCE = Mappers.getMapper(UserRoleResponseMapper.class);

    UserRoleResponse toDto(Role entity);

    List<UserRoleResponse> toDtos(List<Role> entities);
  }
}