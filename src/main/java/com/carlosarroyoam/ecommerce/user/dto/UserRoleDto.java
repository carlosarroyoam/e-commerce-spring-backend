package com.carlosarroyoam.ecommerce.user.dto;

import com.carlosarroyoam.ecommerce.user.entity.UserRole;
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
public class UserRoleDto {
  private Byte id;
  private String type;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface UserRoleDtoMapper {
    UserRoleDtoMapper INSTANCE = Mappers.getMapper(UserRoleDtoMapper.class);

    UserRoleDto toDto(UserRole entity);

    List<UserRoleDto> toDtos(List<UserRole> entities);
  }
}