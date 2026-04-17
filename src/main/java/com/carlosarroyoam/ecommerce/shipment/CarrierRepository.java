package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrierRepository extends JpaRepository<Carrier, Byte> {
}
