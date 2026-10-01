package com.ecommerce.pagamento.service;

import com.ecommerce.pagamento.enums.StatusPagamento;
import com.ecommerce.pagamento.event.PagamentoEvent;
import com.ecommerce.pagamento.event.PedidoCriadoEvent;
import com.ecommerce.pagamento.model.OutboxEvent;
import com.ecommerce.pagamento.model.Pagamento;
import com.ecommerce.pagamento.repository.EventoProcessadoRepository;
import com.ecommerce.pagamento.repository.OutboxEventRepository;
import com.ecommerce.pagamento.repository.PagamentoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
public class PagamentoService {

    private static final BigDecimal LIMITE_SIMULADO = new BigDecimal("1000.00");

    private final EventoProcessadoRepository eventoProcessadoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    public PagamentoService(EventoProcessadoRepository eventoProcessadoRepository,
                            PagamentoRepository pagamentoRepository,
                            OutboxEventRepository outboxEventRepository,
                            JsonMapper jsonMapper) {

        this.eventoProcessadoRepository = eventoProcessadoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public void publicar(PedidoCriadoEvent pedido) {

        int registrado =
                this.eventoProcessadoRepository.registraSeNovo(pedido.eventId());

        if (registrado == 0) {
            log.info("EVENTO_DUPLICADO eventId={} pedido={}"
                    , pedido.eventId(), pedido);
            return;
        }

        boolean aprovado =
                pedido.valor().compareTo(LIMITE_SIMULADO) <= 0;

        StatusPagamento status =
                aprovado ? StatusPagamento.APROVADO :
                        StatusPagamento.RECUSADO;

        String motivo =
                aprovado ? null :
                        "Limite de Pagamento Simulado excedido";

        Pagamento pagamento=new Pagamento(
                pedido.pedidoId(),
                pedido.eventId(),
                pedido.valor(),
                status,
                motivo
        );
        pagamentoRepository.save(pagamento);

        PagamentoEvent evento = new PagamentoEvent(
                UUID.randomUUID().toString(),
                pedido.pedidoId(),
                aprovado?"Pagamento_APROVADO":
                        "Pagamento_RECUSADO",
                pedido.valor(),
                motivo
        );

try{
    String payload = jsonMapper.writeValueAsString(evento);

    OutboxEvent outbox = new OutboxEvent(
            evento.eventId(),
            pedido.pedidoId().toString(),
            evento.tipo(),
            "pagamento",
            payload
    );
    outboxEventRepository.save(outbox);

}catch (JacksonException e){
    throw new IllegalStateException("Erro ao serializar evento"+e);
}
        log.info("PAGAMENTO_PROCESSADO pedidoId={} status={}",
                pedido.pedidoId(), status);


    }
}
