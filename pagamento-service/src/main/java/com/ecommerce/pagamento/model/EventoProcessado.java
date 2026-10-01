package com.ecommerce.pagamento.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_processados")
@Getter
@NoArgsConstructor
public class EventoProcessado {

    @Id
    @Column(name = "event_id",length = 100)
    private String eventId;

    @Column(name = "processado_em",nullable = false)
    private LocalDateTime processadoEm;

}
