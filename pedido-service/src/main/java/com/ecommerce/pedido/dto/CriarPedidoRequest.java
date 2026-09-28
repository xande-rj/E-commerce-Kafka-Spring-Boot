package com.ecommerce.pedido.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CriarPedidoRequest(

        @NotBlank
        String cliente,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal valor

) {
}