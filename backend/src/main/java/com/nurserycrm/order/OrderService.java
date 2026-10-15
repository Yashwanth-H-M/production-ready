package com.nurserycrm.order;

import com.nurserycrm.plantbatch.PlantBatchRepository;
import com.nurserycrm.tenant.TenantAwareExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderLineItemRepository lineItemRepository;
    private final PlantBatchRepository plantBatchRepository;
    private final TenantAwareExecutor executor;

    public OrderService(OrderRepository orderRepository,
                        OrderLineItemRepository lineItemRepository,
                        PlantBatchRepository plantBatchRepository,
                        TenantAwareExecutor executor) {
        this.orderRepository = orderRepository;
        this.lineItemRepository = lineItemRepository;
        this.plantBatchRepository = plantBatchRepository;
        this.executor = executor;
    }

    public Order create(Order order) {
        return executor.execute(() -> orderRepository.save(order));
    }

    public List<Order> findAll() {
        return executor.execute(orderRepository::findAll);
    }

    public Order findById(UUID id) {
        return executor.execute(() -> orderRepository.findById(id).orElse(null));
    }

    public OrderLineItem addLineItem(OrderLineItem item) {
        return executor.execute(() -> {
            if (!orderRepository.existsById(item.getOrderId())) {
                throw new IllegalArgumentException("The selected order is unavailable in this workspace.");
            }
            if (item.getBatchId() != null && !plantBatchRepository.existsById(item.getBatchId())) {
                throw new IllegalArgumentException("The selected plant batch is unavailable in this workspace.");
            }
            return lineItemRepository.save(item);
        });
    }

    public List<OrderLineItem> findLineItemsByOrder(UUID orderId) {
        return executor.execute(() -> lineItemRepository.findByOrderId(orderId));
    }
}
