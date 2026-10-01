package com.ecommerce.pagamento.repository;

import com.ecommerce.pagamento.model.EventoProcessado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoProcessadoRepository extends JpaRepository<EventoProcessado, String> {
    @Modifying
    @Query(value= """
    INSERT INTO evento_processados (event_id,processado_em)
    VALUES (:eventId, CURRENT_TIMESTAMP)
    ON CONFLICT (event_id) DO NOTHING
""", nativeQuery = true)
    int registraSeNovo(@Param("eventId")String eventId);
}
