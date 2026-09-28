package com.ecommerce.pedido.service;

import com.ecommerce.pedido.model.OutboxEvent;
import com.ecommerce.pedido.repository.OutboxEventRepository;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class OutboxPublisher {
    private static final Logger log= LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository repository;
    private final OutboxService outboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository repository, OutboxService outboxService, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.outboxService = outboxService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${outbox.polling-interval:5000}")
    public void publicarEvento(){
        List<OutboxEvent> eventos = repository.findTop100ByPublishedAtIsNullOrderByIdAsc();

        for(OutboxEvent evento:eventos) {
            try {


                SendResult<String, String> resultado =
                        kafkaTemplate.send(
                                evento.getTopic(),
                                evento.getAggregateId(),
                                evento.getPayload()
                        ).get(10, TimeUnit.SECONDS);

                RecordMetadata metadata =
                        resultado.getRecordMetadata();

                outboxService.marcarComoPublicado(evento.getId());

                log.info(
                        "EVENTO_PUBLICADO eventId={} pedidoId={} " +
                                "topic={} partition={} offset={}",
                        evento.getEventId(),
                        evento.getAggregateId(),
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset()
                );
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();

                log.error(
                        "Publicação interrompida. eventId={}",
                        evento.getEventId(),
                        e
                );
                return;


            }catch (ExecutionException | TimeoutException e) {
                log.error(
                        "Falha ao publicar evento. eventId={}",
                        evento.getEventId(),
                        e
                );

                // Mantém este evento pendente e tenta novamente
                // no próximo ciclo.
                break;
            }
        }

    }
}
