package com.carlosarroyoam.ecommerce.customer.dto;

import com.carlosarroyoam.ecommerce.customer.entity.Customer;
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
public class CustomerDto {
  private Long id;
  private UserDto user;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      UserDtoMapper.class })
  public interface CustomerDtoMapper {
    CustomerDtoMapper INSTANCE = Mappers.getMapper(CustomerDtoMapper.class);

    CustomerDto toDto(Customer entity);

    List<CustomerDto> toDtos(List<Customer> entities);
  }
}