package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos para la entidad {@link Carrier}. */
public interface CarrierRepository extends JpaRepository<Carrier, Byte> {
  /**
   * Lista los transportistas que no han sido eliminados.
   *
   * @return los transportistas con {@code deleted_at} nulo
   */
  List<Carrier> findAllByDeletedAtIsNull();
}
