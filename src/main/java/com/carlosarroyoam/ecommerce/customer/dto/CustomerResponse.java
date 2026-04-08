package com.carlosarroyoam.ecommerce.customer.dto;

import com.carlosarroyoam.ecommerce.customer.entity.Customer;
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
public class CustomerResponse {
  private Long id;
  private UserResponse user;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      UserResponseMapper.class })
  public interface CustomerResponseMapper {
    CustomerResponseMapper INSTANCE = Mappers.getMapper(CustomerResponseMapper.class);

    CustomerResponse toDto(Customer entity);

    List<CustomerResponse> toDtos(List<Customer> entities);
  }
}