package com.carlosarroyoam.ecommerce.admin.dto;

import com.carlosarroyoam.ecommerce.admin.entity.Admin;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse.UserResponseMapper;
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
public class AdminResponse {
  private Long id;
  private Boolean isSuper;
  private UserResponse user;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      UserResponseMapper.class })
  public interface AdminResponseMapper {
    AdminResponseMapper INSTANCE = Mappers.getMapper(AdminResponseMapper.class);

    AdminResponse toDto(Admin entity);

    List<AdminResponse> toDtos(List<Admin> entities);
  }
}