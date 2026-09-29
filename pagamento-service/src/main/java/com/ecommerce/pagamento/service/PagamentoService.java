package com.ecommerce.pagamento.service;

import com.ecommerce.pagamento.event.PagamentoEvent;
import com.ecommerce.pagamento.event.PedidoCriadoEvent;
import com.ecommerce.pagamento.producer.PagamentoProducer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PagamentoService {

    private static final BigDecimal LIMITE_SIMULADO = new BigDecimal("1000.00");
    private final PagamentoProducer pagamentoProducer;

    public PagamentoService(PagamentoProducer pagamentoProducer) {
        this.pagamentoProducer = pagamentoProducer;
    }

    public void publicar(PedidoCriadoEvent pedido){
        boolean aprovado = pedido.valor().compareTo(LIMITE_SIMULADO) <= 0;

        String tipo = aprovado ? "PAGAMENTO_APROVADO" : "PAGAMENTO_RECUSADO";

        String motivo = aprovado?null:"Limite de Pagamento Simulado excedido";

        String eventId = UUID.nameUUIDFromBytes(("pagamento:"+pedido.eventId()).getBytes()).toString();

        PagamentoEvent evento = new PagamentoEvent(
                eventId,
                pedido.pedidoId(),
                tipo,
                pedido.valor(),
                motivo
        );

        pagamentoProducer.publicar(evento);

    }
}
