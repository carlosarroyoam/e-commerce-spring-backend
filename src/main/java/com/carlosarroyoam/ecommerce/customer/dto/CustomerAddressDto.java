package com.carlosarroyoam.ecommerce.customer.dto;

import com.carlosarroyoam.ecommerce.customer.dto.CustomerDto.CustomerDtoMapper;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
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
public class CustomerAddressDto {
  private Long id;
  private String streetName;
  private String streetNumber;
  private String apartamentNumber;
  private String sublocality;
  private String locality;
  private String state;
  private String postalCode;
  private String country;
  private String phoneNumber;
  private CustomerDto customer;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      CustomerDtoMapper.class })
  public interface CustomerAddressDtoMapper {
    CustomerAddressDtoMapper INSTANCE = Mappers.getMapper(CustomerAddressDtoMapper.class);

    CustomerAddressDto toDto(CustomerAddress entity);

    List<CustomerAddressDto> toDtos(List<CustomerAddress> entities);
  }
}