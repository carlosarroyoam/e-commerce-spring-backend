package com.carlosarroyoam.ecommerce.customer.dto;

import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse.CustomerAddressResponseMapper;
import com.carlosarroyoam.ecommerce.customer.entity.Customer;
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
public class CustomerSummaryResponse {
  private Long id;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String email;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      CustomerAddressResponseMapper.class })
  public interface CustomerSummaryResponseMapper {
    CustomerSummaryResponseMapper INSTANCE = Mappers.getMapper(CustomerSummaryResponseMapper.class);

    CustomerSummaryResponse toDto(Customer entity);

    List<CustomerSummaryResponse> toDtos(List<Customer> entities);
  }
}