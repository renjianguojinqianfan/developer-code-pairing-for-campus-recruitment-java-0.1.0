package com.example.demo.domain.order;

import java.util.Optional;

/**
 * Repository interface for Order aggregate.
 * This is a domain interface, implementations are provided by infrastructure layer.
 */
public interface OrderRepository {
    /**
     * Save an order.
     * @param order the order to save
     */
    void save(Order order);

    /**
     * Find an order by its ID.
     * @param orderId the order ID
     * @return Optional containing the order if found
     */
    Optional<Order> findById(OrderId orderId);
}
