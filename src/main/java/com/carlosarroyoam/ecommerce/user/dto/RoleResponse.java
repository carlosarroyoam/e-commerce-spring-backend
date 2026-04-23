package com.carlosarroyoam.ecommerce.user.dto;

import com.carlosarroyoam.ecommerce.user.entity.Role;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Getter
@Setter
@Builder
public class RoleResponse {
  private Byte id;
  private String name;
  private String description;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface RoleResponseMapper {
    RoleResponseMapper INSTANCE = Mappers.getMapper(RoleResponseMapper.class);

    RoleResponse toDto(Role entity);

    List<RoleResponse> toDtos(List<Role> entities);
  }
}
