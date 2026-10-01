package com.ecommerce.pagamento.service;

import com.ecommerce.pagamento.model.OutboxEvent;
import com.ecommerce.pagamento.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PagamentoOutboxService {
    private final OutboxEventRepository outboxEventRepository;
    public PagamentoOutboxService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }
    @Transactional
    public void marcaComoPublicado(Long id){
        OutboxEvent outboxEvent = outboxEventRepository.findById(id).
                orElseThrow(()->
                        new IllegalArgumentException("Evento nao encontrado"+id));

        outboxEvent.marcarComoPublicado();
    }
}
