package com.ecommerce.pagamento.service;

import com.ecommerce.pagamento.model.OutboxEvent;
import com.ecommerce.pagamento.repository.OutboxEventRepository;
import org.apache.kafka.clients.producer.RecordMetadata;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final PagamentoOutboxService pagamentoOutboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate, PagamentoOutboxService pagamentoOutboxService) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.pagamentoOutboxService = pagamentoOutboxService;
    }

    @Scheduled(fixedDelayString = "${outbox.polling-interval:5000}")
    public void publicarEvento() {
        List<OutboxEvent> eventos = outboxEventRepository.findTop100ByPublishedAtIsNullOrderByIdAsc();
        for (OutboxEvent evento : eventos) {
            try {
                SendResult<String, String> result =
                        kafkaTemplate.send(
                                evento.getTopic(),
                                evento.getAggregateId(),
                                evento.getPayload()
                        ).get(10, TimeUnit.SECONDS);

                RecordMetadata metadata = result.getRecordMetadata();
                pagamentoOutboxService.marcaComoPublicado(evento.getId());

                log.info("PAGAMENTO_EVENTO_PUBLICADO eventoId={} " +
                                "pedidoId={} topic={} partition={} offset={}",
                        evento.getEventId(),
                        evento.getAggregateId(),
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset()
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Publicacao interrompida");
                return;
            } catch (ExecutionException | TimeoutException e) {
                log.error(
                        "Error ao publicar evento eventoId={}",
                        evento.getEventId()+
                                e
                );
                break;
            }
        }
    }
}
