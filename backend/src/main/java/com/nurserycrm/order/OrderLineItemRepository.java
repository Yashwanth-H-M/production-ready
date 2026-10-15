package com.nurserycrm.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderLineItemRepository extends JpaRepository<OrderLineItem, UUID> {
    List<OrderLineItem> findByOrderId(UUID orderId);
}
