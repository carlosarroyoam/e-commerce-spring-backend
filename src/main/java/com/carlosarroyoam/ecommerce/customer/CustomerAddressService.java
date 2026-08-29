package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse.CustomerAddressResponseMapper;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lógica de negocio para consultar direcciones de clientes ({@link CustomerAddress}). */
@Service
public class CustomerAddressService {
  private final CustomerRepository customerRepository;
  private final CustomerAddressRepository customerAddressRepository;

  public CustomerAddressService(
      final CustomerRepository customerRepository,
      final CustomerAddressRepository customerAddressRepository) {
    this.customerRepository = customerRepository;
    this.customerAddressRepository = customerAddressRepository;
  }

  /**
   * Lista todas las direcciones de un cliente.
   *
   * @param customerId el id del cliente
   * @return la lista de {@link CustomerAddressResponse}
   * @throws ResourceNotFoundException con 404 si no existe un cliente con ese id
   */
  @Transactional(readOnly = true)
  public List<CustomerAddressResponse> findAllByCustomerId(Long customerId) {
    validateCustomerExists(customerId);

    List<CustomerAddress> addresses = customerAddressRepository.findAllByCustomerId(customerId);

    return CustomerAddressResponseMapper.INSTANCE.toDtos(addresses);
  }

  /**
   * Busca una dirección de un cliente por su id.
   *
   * @param customerId el id del cliente
   * @param addressId el id de la dirección
   * @return el {@link CustomerAddressResponse} correspondiente
   * @throws ResourceNotFoundException con 404 si no existe el cliente o la dirección
   */
  @Transactional(readOnly = true)
  public CustomerAddressResponse findById(Long customerId, Long addressId) {
    validateCustomerExists(customerId);

    CustomerAddress customerAddressById = findCustomerAddressByIdOrFail(customerId, addressId);

    return CustomerAddressResponseMapper.INSTANCE.toDto(customerAddressById);
  }

  /**
   * Verifica que exista un cliente con el id indicado.
   *
   * @param customerId el id del cliente
   * @throws ResourceNotFoundException con 404 si no existe un cliente con ese id
   */
  private void validateCustomerExists(Long customerId) {
    if (!customerRepository.existsById(customerId)) {
      throw new ResourceNotFoundException(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
    }
  }

  /**
   * Busca una dirección de un cliente por su id o lanza una excepción si no existe.
   *
   * @param customerId el id del cliente
   * @param addressId el id de la dirección
   * @return la {@link CustomerAddress} encontrada
   * @throws ResourceNotFoundException con 404 si no existe una dirección con ese id para ese
   *     cliente
   */
  private CustomerAddress findCustomerAddressByIdOrFail(Long customerId, Long addressId) {
    return customerAddressRepository
        .findByIdAndCustomerId(addressId, customerId)
        .orElseThrow(
            () -> new ResourceNotFoundException(AppMessages.CUSTOMER_ADDRESS_NOT_FOUND_EXCEPTION));
  }
}
