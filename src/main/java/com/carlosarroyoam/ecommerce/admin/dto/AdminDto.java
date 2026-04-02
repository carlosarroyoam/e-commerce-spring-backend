package com.carlosarroyoam.ecommerce.admin.dto;

import com.carlosarroyoam.ecommerce.admin.entity.Admin;
import com.carlosarroyoam.ecommerce.user.dto.UserDto;
import com.carlosarroyoam.ecommerce.user.dto.UserDto.UserDtoMapper;
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
public class AdminDto {
  private Long id;
  private Boolean isSuper;
  private UserDto user;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      UserDtoMapper.class })
  public interface AdminDtoMapper {
    AdminDtoMapper INSTANCE = Mappers.getMapper(AdminDtoMapper.class);

    AdminDto toDto(Admin entity);

    List<AdminDto> toDtos(List<Admin> entities);
  }
}