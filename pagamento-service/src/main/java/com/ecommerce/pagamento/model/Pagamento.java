package com.ecommerce.pagamento.model;

import com.ecommerce.pagamento.enums.StatusPagamento;
import jakarta.persistence.*;
import lombok.*;



import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
@Getter
@AllArgsConstructor
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id",nullable = false, unique = true)
    private Long pedidoId;

    @Column(name = "pedido_event_id",nullable = false, unique = true)
    private String pedidoEventId;

    @Column(name = "valor",nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status;

    private String motivo;

    @Column(name = "criado_em",nullable = false)
    private LocalDateTime criadoEm;


    public Pagamento(Long pedidoId, String s, BigDecimal valor, StatusPagamento status, String motivo) {
        this.pedidoId = pedidoId;
        this.pedidoEventId = s;
        this.valor = valor;
        this.status = status;
        this.motivo = motivo;
    }
}
