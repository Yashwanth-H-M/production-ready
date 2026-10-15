package com.nurserycrm.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setOrderType(request.orderType());
        return ResponseEntity.ok(service.create(order));
    }

    @GetMapping
    public ResponseEntity<List<Order>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable UUID id) {
        Order order = service.findById(id);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/line-items")
    public ResponseEntity<OrderLineItem> addLineItem(@PathVariable UUID id, @Valid @RequestBody LineItemRequest request) {
        OrderLineItem item = new OrderLineItem();
        item.setOrderId(id);
        item.setBatchId(request.batchId());
        item.setSpecies(request.species());
        item.setPromisedQty(request.promisedQty());
        item.setPromisedDate(request.promisedDate());
        return ResponseEntity.ok(service.addLineItem(item));
    }

    @GetMapping("/{id}/line-items")
    public ResponseEntity<List<OrderLineItem>> listLineItems(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findLineItemsByOrder(id));
    }

    public record OrderRequest(@NotBlank String customerName, @NotBlank String orderType) {}

    public record LineItemRequest(
        UUID batchId,
        @NotBlank String species,
        @NotNull @Positive Integer promisedQty,
        LocalDate promisedDate
    ) {}
}
