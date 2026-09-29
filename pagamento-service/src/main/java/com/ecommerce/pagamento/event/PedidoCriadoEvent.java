package com.ecommerce.pagamento.event;

import java.math.BigDecimal;

public record PedidoCriadoEvent(
        String eventId,
        Long pedidoId,
        String tipo,
        String cliente,
        BigDecimal valor
) {
}