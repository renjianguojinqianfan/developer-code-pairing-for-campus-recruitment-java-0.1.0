package com.example.demo.infrastructure.persistence.order.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEntityRepository extends JpaRepository<OrderEntity, String> {}
