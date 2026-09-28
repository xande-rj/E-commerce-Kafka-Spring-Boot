package com.ecommerce.pedido.event;


import java.math.BigDecimal;

public record PedidoCriadoEvent(
        String eventId,
        Long pedidoId,
        String tipo,
        String cliente,
        BigDecimal valor
) {
}