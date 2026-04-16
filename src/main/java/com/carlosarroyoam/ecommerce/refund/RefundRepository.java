package com.carlosarroyoam.ecommerce.refund;

import com.carlosarroyoam.ecommerce.refund.entity.Refund;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RefundRepository
    extends JpaRepository<Refund, Long>, JpaSpecificationExecutor<Refund> {
  Optional<Refund> findByOrderId(Long orderId);
}
