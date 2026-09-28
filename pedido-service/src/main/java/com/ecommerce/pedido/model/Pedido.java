package com.ecommerce.pedido.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cliente;

    @Column(nullable = false,precision = 12,scale=2)
    private BigDecimal valor;

    @Column(name = "criado_em" ,nullable = false)
    private LocalDateTime criadoEm;
    public Pedido() {
    }

    public Pedido(String cliente, BigDecimal valor) {
        this.cliente = cliente;
        this.valor = valor;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
