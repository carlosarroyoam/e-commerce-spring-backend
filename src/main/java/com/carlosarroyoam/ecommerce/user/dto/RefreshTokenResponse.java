package com.carlosarroyoam.ecommerce.user.dto;

import com.carlosarroyoam.ecommerce.user.dto.UserResponse.UserResponseMapper;
import com.carlosarroyoam.ecommerce.user.entity.RefreshToken;
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
public class RefreshTokenResponse {
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
  public interface RefreshTokenResponseMapper {
    RefreshTokenResponseMapper INSTANCE = Mappers.getMapper(RefreshTokenResponseMapper.class);

    RefreshTokenResponse toDto(RefreshToken entity);

    List<RefreshTokenResponse> toDtos(List<RefreshToken> entities);
  }
}