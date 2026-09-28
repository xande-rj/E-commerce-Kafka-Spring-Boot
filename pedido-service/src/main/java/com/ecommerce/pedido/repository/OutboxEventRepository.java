package com.ecommerce.pedido.repository;

import com.ecommerce.pedido.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {
}