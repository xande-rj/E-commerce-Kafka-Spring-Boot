package com.ecommerce.pedido.service;

import com.ecommerce.pedido.dto.CriarPedidoRequest;
import com.ecommerce.pedido.event.PedidoCriadoEvent;
import com.ecommerce.pedido.model.OutboxEvent;
import com.ecommerce.pedido.model.Pedido;
import com.ecommerce.pedido.repository.OutboxEventRepository;
import com.ecommerce.pedido.repository.PedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


import java.util.UUID;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public PedidoService(
            PedidoRepository pedidoRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.pedidoRepository = pedidoRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Pedido criarPedido(CriarPedidoRequest request) {

        Pedido pedido = new Pedido(
                request.cliente(),
                request.valor()
        );

        pedidoRepository.save(pedido);

        String eventId = UUID.randomUUID().toString();

        PedidoCriadoEvent event = new PedidoCriadoEvent(
                eventId,
                pedido.getId(),
                "PEDIDO_CRIADO",
                pedido.getCliente(),
                pedido.getValor()
        );



            String payload =
                    objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(
                    event.eventId(),
                    event.pedidoId().toString(),
                    event.tipo(),
                    "pedidos",
                    payload
            );

            outboxEventRepository.save(outboxEvent);




        return pedido;
    }
}
