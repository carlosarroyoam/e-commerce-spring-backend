package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos para la entidad {@link Shipment}. */
public interface ShipmentRepository
    extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {
  /**
   * Busca el envío asociado a una orden.
   *
   * @param orderId el id de la orden
   * @return el envío encontrado, o {@link Optional#empty()} si la orden no tiene envío asociado
   */
  Optional<Shipment> findByOrderId(Long orderId);
}
