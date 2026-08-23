package com.carlosarroyoam.ecommerce.order;

import com.carlosarroyoam.ecommerce.order.entity.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos para la entidad {@link Order}. */
public interface OrderRepository
    extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
  /**
   * Busca una orden por su número de orden.
   *
   * @param orderNumber el número de la orden
   * @return la orden encontrada, o {@link Optional#empty()} si no existe ninguna con ese número
   */
  Optional<Order> findByOrderNumber(String orderNumber);
}
