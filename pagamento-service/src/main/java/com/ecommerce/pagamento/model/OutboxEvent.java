package com.ecommerce.pagamento.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public OutboxEvent(String s, String string, String tipo, String pagamento, String payload) {
        this.eventId = s;
        this.aggregateId = string;
        this.eventType = tipo;
        this.topic = pagamento;
        this.payload = payload;
    }

    public void marcarComoPublicado() {
        this.publishedAt = LocalDateTime.now();
    }

}
