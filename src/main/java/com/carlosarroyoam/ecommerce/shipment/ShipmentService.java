package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.shipment.dto.CarrierResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.CarrierResponse.CarrierResponseMapper;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentResponse.ShipmentResponseMapper;
import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShipmentService {
  private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);
  private final ShipmentRepository shipmentRepository;
  private final CarrierRepository carrierRepository;

  public ShipmentService(final ShipmentRepository shipmentRepository,
      final CarrierRepository carrierRepository) {
    this.shipmentRepository = shipmentRepository;
    this.carrierRepository = carrierRepository;
  }

  @Transactional(readOnly = true)
  public PagedResponse<ShipmentResponse> findAll(Pageable pageable) {
    Page<Shipment> shipments = shipmentRepository.findAll(pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(shipments.map(ShipmentResponseMapper.INSTANCE::toDto));
  }

  @Transactional(readOnly = true)
  public ShipmentResponse findById(Long shipmentId) {
    Shipment shipment = findShipmentByIdOrFail(shipmentId);
    return ShipmentResponseMapper.INSTANCE.toDto(shipment);
  }

  @Transactional(readOnly = true)
  public ShipmentResponse findByOrderId(Long orderId) {
    Shipment shipment = shipmentRepository.findByOrderId(orderId).orElseThrow(() -> {
      log.warn(AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
    });
    return ShipmentResponseMapper.INSTANCE.toDto(shipment);
  }

  @Transactional(readOnly = true)
  public List<CarrierResponse> findAllActiveCarriers() {
    List<Carrier> carriers = carrierRepository.findAllByIsActiveTrue();
    return CarrierResponseMapper.INSTANCE.toDtos(carriers);
  }

  private Shipment findShipmentByIdOrFail(Long shipmentId) {
    return shipmentRepository.findById(shipmentId).orElseThrow(() -> {
      log.warn(AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
    });
  }
}
