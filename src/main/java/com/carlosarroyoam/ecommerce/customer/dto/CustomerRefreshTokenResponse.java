package com.carlosarroyoam.ecommerce.customer.dto;

import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse.CustomerResponseMapper;
import com.carlosarroyoam.ecommerce.user.entity.UserRefreshToken;
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
public class CustomerRefreshTokenResponse {
  private Long id;
  private String token;
  private String fingerprint;
  private CustomerResponse customer;
  private LocalDateTime lastUsedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      CustomerResponseMapper.class })
  public interface CustomerRefreshTokenResponseMapper {
    CustomerRefreshTokenResponseMapper INSTANCE = Mappers
        .getMapper(CustomerRefreshTokenResponseMapper.class);

    CustomerRefreshTokenResponse toDto(UserRefreshToken entity);

    List<CustomerRefreshTokenResponse> toDtos(List<UserRefreshToken> entities);
  }
}