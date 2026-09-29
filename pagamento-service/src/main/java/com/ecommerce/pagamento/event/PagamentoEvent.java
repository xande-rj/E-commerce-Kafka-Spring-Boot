package com.ecommerce.pagamento.event;

import java.math.BigDecimal;

public record PagamentoEvent(
        String eventId,
        Long pedidoId,
        String tipo,
        BigDecimal valor,
        String motivo
) {
}