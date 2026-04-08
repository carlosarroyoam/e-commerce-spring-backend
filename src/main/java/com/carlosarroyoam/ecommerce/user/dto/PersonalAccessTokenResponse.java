package com.carlosarroyoam.ecommerce.user.dto;

import com.carlosarroyoam.ecommerce.user.dto.UserResponse.UserResponseMapper;
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
public class PersonalAccessTokenResponse {
  private Long id;
  private String token;
  private LocalDateTime lastUsedAt;
  private String fingerprint;
  private String userAgent;
  private UserResponse user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      UserResponseMapper.class })
  public interface PersonalAccessTokenResponseMapper {
    PersonalAccessTokenResponseMapper INSTANCE = Mappers
        .getMapper(PersonalAccessTokenResponseMapper.class);

    PersonalAccessTokenResponse toDto(PersonalAccessToken entity);

    List<PersonalAccessTokenResponse> toDtos(List<PersonalAccessToken> entities);
  }
}