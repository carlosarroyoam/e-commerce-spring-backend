package com.carlosarroyoam.ecommerce.user.dto;

import com.carlosarroyoam.ecommerce.auth.entity.RefreshToken;
import com.carlosarroyoam.ecommerce.user.dto.UserResponse.UserResponseMapper;
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
public class UserRefreshTokenResponse {
  private Long id;
  private String token;
  private String fingerprint;
  private UserResponse user;
  private LocalDateTime lastUsedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      UserResponseMapper.class })
  public interface RefreshTokenResponseMapper {
    RefreshTokenResponseMapper INSTANCE = Mappers.getMapper(RefreshTokenResponseMapper.class);

    UserRefreshTokenResponse toDto(RefreshToken entity);

    List<UserRefreshTokenResponse> toDtos(List<RefreshToken> entities);
  }
}