package com.ecommerce.pedido.controller;

import com.ecommerce.pedido.dto.CriarPedidoRequest;
import com.ecommerce.pedido.model.Pedido;
import com.ecommerce.pedido.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<Pedido> criar(
            @Valid @RequestBody CriarPedidoRequest request
    ) {

        Pedido pedido =
                pedidoService.criarPedido(request);

        return ResponseEntity.ok(pedido);
    }
}
