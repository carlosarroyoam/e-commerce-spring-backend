package com.carlosarroyoam.ecommerce.user.dto;

import com.carlosarroyoam.ecommerce.user.dto.UserDto.UserDtoMapper;
import com.carlosarroyoam.ecommerce.user.entity.PersonalAccessToken;
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
public class PersonalAccessTokenDto {
  private Long id;
  private String token;
  private LocalDateTime lastUsedAt;
  private String fingerprint;
  private String userAgent;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      UserDtoMapper.class })
  public interface PersonalAccessTokenDtoMapper {
    PersonalAccessTokenDtoMapper INSTANCE = Mappers.getMapper(PersonalAccessTokenDtoMapper.class);

    PersonalAccessTokenDto toDto(PersonalAccessToken entity);

    List<PersonalAccessTokenDto> toDtos(List<PersonalAccessToken> entities);
  }
}