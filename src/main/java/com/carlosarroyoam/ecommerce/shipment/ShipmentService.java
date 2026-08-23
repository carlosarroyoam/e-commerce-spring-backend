package com.carlosarroyoam.ecommerce.shipment;

import com.carlosarroyoam.ecommerce.core.constant.AppMessages;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse;
import com.carlosarroyoam.ecommerce.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.ecommerce.core.specification.SpecificationBuilder;
import com.carlosarroyoam.ecommerce.order.entity.Order_;
import com.carlosarroyoam.ecommerce.shipment.dto.CarrierResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.CarrierResponse.CarrierResponseMapper;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentResponse;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentResponse.ShipmentResponseMapper;
import com.carlosarroyoam.ecommerce.shipment.dto.ShipmentSpecs;
import com.carlosarroyoam.ecommerce.shipment.entity.Carrier;
import com.carlosarroyoam.ecommerce.shipment.entity.Shipment;
import com.carlosarroyoam.ecommerce.shipment.entity.Shipment_;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Lógica de negocio para consultar envíos ({@link Shipment}) y transportistas ({@link Carrier}). */
@Service
public class ShipmentService {
  private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);
  private final ShipmentRepository shipmentRepository;
  private final CarrierRepository carrierRepository;

  public ShipmentService(
      final ShipmentRepository shipmentRepository, final CarrierRepository carrierRepository) {
    this.shipmentRepository = shipmentRepository;
    this.carrierRepository = carrierRepository;
  }

  /**
   * Obtiene una página de envíos que cumplen los filtros indicados.
   *
   * @param shipmentSpecs los filtros de búsqueda
   * @param pageable la paginación y el orden a aplicar
   * @return la página de {@link ShipmentResponse} resultante
   */
  @Transactional(readOnly = true)
  public PagedResponse<ShipmentResponse> findAll(ShipmentSpecs shipmentSpecs, Pageable pageable) {
    Specification<Shipment> spec =
        SpecificationBuilder.<Shipment>builder()
            .equalsIfPresent(
                root -> root.join(Shipment_.order).get(Order_.id), shipmentSpecs.getOrderId())
            .build();

    Page<Shipment> shipments = shipmentRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        shipments.map(ShipmentResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un envío por su id.
   *
   * @param shipmentId el id del envío
   * @return el {@link ShipmentResponse} correspondiente
   * @throws ResponseStatusException con 404 si no existe un envío con ese id
   */
  @Transactional(readOnly = true)
  public ShipmentResponse findById(Long shipmentId) {
    Shipment shipmentById = findShipmentByIdOrFail(shipmentId);
    return ShipmentResponseMapper.INSTANCE.toDto(shipmentById);
  }

  /**
   * Busca el envío asociado a una orden.
   *
   * @param orderId el id de la orden
   * @return el {@link ShipmentResponse} correspondiente
   * @throws ResponseStatusException con 404 si la orden no tiene un envío asociado
   */
  @Transactional(readOnly = true)
  public ShipmentResponse findByOrderId(Long orderId) {
    Shipment shipmentByOrderId = findShipmentByOrderIdOrFail(orderId);
    return ShipmentResponseMapper.INSTANCE.toDto(shipmentByOrderId);
  }

  /**
   * Lista todos los transportistas disponibles.
   *
   * @return la lista de {@link CarrierResponse}
   */
  @Transactional(readOnly = true)
  public List<CarrierResponse> findAllActiveCarriers() {
    List<Carrier> carriers = carrierRepository.findAll();
    return CarrierResponseMapper.INSTANCE.toDtos(carriers);
  }

  /**
   * Busca un envío por su id o lanza una excepción si no existe.
   *
   * @param shipmentId el id del envío
   * @return el {@link Shipment} encontrado
   * @throws ResponseStatusException con 404 si no existe un envío con ese id
   */
  private Shipment findShipmentByIdOrFail(Long shipmentId) {
    return shipmentRepository
        .findById(shipmentId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
            });
  }

  /**
   * Busca el envío asociado a una orden o lanza una excepción si no existe.
   *
   * @param orderId el id de la orden
   * @return el {@link Shipment} encontrado
   * @throws ResponseStatusException con 404 si la orden no tiene un envío asociado
   */
  private Shipment findShipmentByOrderIdOrFail(Long orderId) {
    return shipmentRepository
        .findByOrderId(orderId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
            });
  }
}
