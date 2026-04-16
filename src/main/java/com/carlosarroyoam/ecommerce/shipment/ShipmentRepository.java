package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ShipmentRepository
    extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {
  Optional<Shipment> findByOrderId(Long orderId);
}
