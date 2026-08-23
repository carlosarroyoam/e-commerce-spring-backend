package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.customer.entity.CustomerAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos para la entidad {@link CustomerAddress}. */
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
  /**
   * Lista todas las direcciones de un cliente.
   *
   * @param customerId el id del cliente
   * @return las direcciones encontradas
   */
  List<CustomerAddress> findAllByCustomerId(Long customerId);

  /**
   * Busca una dirección por su id, restringida a un cliente específico.
   *
   * @param addressId el id de la dirección
   * @param customerId el id del cliente propietario
   * @return la dirección encontrada, o {@link Optional#empty()} si no existe o no pertenece a ese
   *     cliente
   */
  Optional<CustomerAddress> findByIdAndCustomerId(Long addressId, Long customerId);
}
