package com.ecommerce.pedido.service;

import com.ecommerce.pedido.model.OutboxEvent;
import com.ecommerce.pedido.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {
    private final OutboxEventRepository repository;

    public OutboxService(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void marcarComoPublicado(Long id) {
        OutboxEvent evento = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Evento não encontrado: " + id
                        )
                );

        evento.marcarComoPublicado();
    }
}
