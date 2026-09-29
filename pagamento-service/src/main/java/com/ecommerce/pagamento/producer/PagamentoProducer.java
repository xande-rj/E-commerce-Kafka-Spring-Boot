package com.ecommerce.pagamento.producer;

import com.ecommerce.pagamento.event.PagamentoEvent;
import org.apache.kafka.clients.producer.RecordMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class PagamentoProducer {
    private static final Logger log = LoggerFactory.getLogger(PagamentoProducer.class);
    private static final String TOPIC= "pagamento";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    public PagamentoProducer(KafkaTemplate<String, String> kafkaTemplate, JsonMapper jsonMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
    }

    public void publicar(PagamentoEvent event){
        String payload = jsonMapper.writeValueAsString(event);

        try {
            SendResult<String,String> resultado = kafkaTemplate.send(
                    TOPIC,
                    event.eventId().toString(),
                    payload
            ).get(10, TimeUnit.SECONDS);
            RecordMetadata metadata = resultado.getRecordMetadata();
            log.info(
                    "PAGAMENTO_PUBLICADO eventId={} pedidoId={} " +
                            "topic={} partition={} offset={}",
                    event.eventId(),
                    event.pedidoId(),
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset()
            );

        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Publicação do pagamento interrompida",
                    e
            );

        } catch ( ExecutionException | TimeoutException e){
            throw new IllegalStateException(
                    "Erro ao publicar pagamento no Kafka",
                    e
            );
        }
    }
}
