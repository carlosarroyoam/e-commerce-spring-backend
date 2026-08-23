package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos para la entidad {@link Carrier}. */
public interface CarrierRepository extends JpaRepository<Carrier, Byte> {}
