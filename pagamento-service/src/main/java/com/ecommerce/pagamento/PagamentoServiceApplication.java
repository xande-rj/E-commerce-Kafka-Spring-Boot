package com.ecommerce.pagamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PagamentoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PagamentoServiceApplication.class, args);
    }
}
