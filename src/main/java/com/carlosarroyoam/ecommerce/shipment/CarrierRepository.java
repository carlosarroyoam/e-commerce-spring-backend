package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrierRepository extends JpaRepository<Carrier, Byte> {
  List<Carrier> findAllByIsActiveTrue();
}
