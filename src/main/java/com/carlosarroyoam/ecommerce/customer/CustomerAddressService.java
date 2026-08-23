package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse;
import com.carlosarroyoam.ecommerce.customer.dto.CustomerAddressResponse.CustomerAddressResponseMapper;
import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Lógica de negocio para consultar direcciones de clientes ({@link CustomerAddress}). */
@Service
public class CustomerAddressService {
  private static final Logger log = LoggerFactory.getLogger(CustomerAddressService.class);
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
   * @throws ResponseStatusException con 404 si no existe un cliente con ese id
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
   * @throws ResponseStatusException con 404 si no existe el cliente o la dirección
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
   * @throws ResponseStatusException con 404 si no existe un cliente con ese id
   */
  private void validateCustomerExists(Long customerId) {
    if (!customerRepository.existsById(customerId)) {
      log.warn(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
    }
  }

  /**
   * Busca una dirección de un cliente por su id o lanza una excepción si no existe.
   *
   * @param customerId el id del cliente
   * @param addressId el id de la dirección
   * @return la {@link CustomerAddress} encontrada
   * @throws ResponseStatusException con 404 si no existe una dirección con ese id para ese cliente
   */
  private CustomerAddress findCustomerAddressByIdOrFail(Long customerId, Long addressId) {
    return customerAddressRepository
        .findByIdAndCustomerId(addressId, customerId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.CUSTOMER_ADDRESS_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.CUSTOMER_ADDRESS_NOT_FOUND_EXCEPTION);
            });
  }
}
