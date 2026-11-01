package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos para la entidad {@link Shipment}. */
public interface ShipmentRepository
    extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {
  /**
   * Lista los envíos asociados a una orden.
   *
   * @param orderId el id de la orden
   * @return los envíos de la orden, o una lista vacía si no tiene ninguno
   */
  List<Shipment> findAllByOrderId(Long orderId);
}
