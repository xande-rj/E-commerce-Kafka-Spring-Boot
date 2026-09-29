package com.ecommerce.pagamento.consumer;

import com.ecommerce.pagamento.event.PedidoCriadoEvent;
import com.ecommerce.pagamento.service.PagamentoService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PedidoConsumer {
    private static final Logger log = LoggerFactory.getLogger(PedidoConsumer.class);
    private final JsonMapper jsonMapper;
    private final PagamentoService pagamentoService;

    public PedidoConsumer(JsonMapper jsonMapper, PagamentoService pagamentoService) {
        this.jsonMapper = jsonMapper;
        this.pagamentoService = pagamentoService;
    }

    @KafkaListener(
            topics = "pedidos",
            groupId = "pagamento-group"
    )
    public void consumir(ConsumerRecord<String, String> record) {
        PedidoCriadoEvent pedido = jsonMapper.readValue(
                record.value(),
                PedidoCriadoEvent.class
        );
        log.info(
                "PEDIDO_RECEBIDO eventId={} pedidoId={}"+
                        "topic={} partition={} offfset={}",
                pedido.eventId(),
                pedido.pedidoId(),
                record.topic(),
                record.partition(),
                record.offset()
        );
        pagamentoService.publicar(pedido);

    }
}
