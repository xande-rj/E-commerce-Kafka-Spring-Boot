package com.ecommerce.pagamento.repository;

import com.ecommerce.pagamento.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByPublishedAtIsNullOrderByIdAsc();
}
