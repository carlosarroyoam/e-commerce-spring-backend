package com.carlosarroyoam.ecommerce.order;

import com.carlosarroyoam.ecommerce.order.entity.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
  Optional<Order> findByOrderNumber(String orderNumber);
}
