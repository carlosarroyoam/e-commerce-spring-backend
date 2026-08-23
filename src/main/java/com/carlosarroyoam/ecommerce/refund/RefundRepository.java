package com.carlosarroyoam.ecommerce.refund;

import com.carlosarroyoam.ecommerce.refund.entity.Refund;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos para la entidad {@link Refund}. */
public interface RefundRepository
    extends JpaRepository<Refund, Long>, JpaSpecificationExecutor<Refund> {
  /**
   * Busca el reembolso asociado a una orden.
   *
   * @param orderId el id de la orden
   * @return el reembolso encontrado, o {@link Optional#empty()} si la orden no tiene reembolso
   *     asociado
   */
  Optional<Refund> findByOrderId(Long orderId);
}
