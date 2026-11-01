package com.carlosarroyoam.ecommerce.refund;

import com.carlosarroyoam.ecommerce.refund.entity.Refund;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos para la entidad {@link Refund}. */
public interface RefundRepository
    extends JpaRepository<Refund, Long>, JpaSpecificationExecutor<Refund> {
  /**
   * Lista los reembolsos asociados a una orden.
   *
   * @param orderId el id de la orden
   * @return los reembolsos de la orden, o una lista vacía si no tiene ninguno
   */
  List<Refund> findAllByOrderId(Long orderId);
}
