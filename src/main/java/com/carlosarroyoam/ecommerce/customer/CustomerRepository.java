package com.carlosarroyoam.ecommerce.customer;

import com.carlosarroyoam.ecommerce.customer.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos para la entidad {@link Customer}. */
public interface CustomerRepository
    extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
  /**
   * Busca un cliente por su email.
   *
   * @param email el email del cliente
   * @return el cliente encontrado, o {@link Optional#empty()} si no existe ninguno con ese email
   */
  Optional<Customer> findByEmail(String email);
}
