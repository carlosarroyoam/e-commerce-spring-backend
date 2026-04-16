package com.carlosarroyoam.ecommerce.customer.dto;

import com.carlosarroyoam.ecommerce.customer.dto.CustomerResponse.CustomerResponseMapper;
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
public class CustomerAddressResponse {
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
  private Boolean isDefault;
  private CustomerResponse customer;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      CustomerResponseMapper.class })
  public interface CustomerAddressResponseMapper {
    CustomerAddressResponseMapper INSTANCE = Mappers.getMapper(CustomerAddressResponseMapper.class);

    CustomerAddressResponse toDto(CustomerAddress entity);

    List<CustomerAddressResponse> toDtos(List<CustomerAddress> entities);
  }
}