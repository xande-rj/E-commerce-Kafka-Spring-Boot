🛒 E-commerce Event-Driven com Spring Boot e Kafka

Projeto de estudo e portfólio desenvolvido para praticar arquitetura orientada a eventos (Event-Driven Architecture) utilizando Java, Spring Boot, Apache Kafka e PostgreSQL.

O projeto simula um fluxo de e-commerce em que a criação de um pedido gera eventos que podem ser consumidos por diferentes serviços, como pagamento, estoque e notificações.

🚧 Status atual: em desenvolvimento.

Neste momento, o fluxo de Pedido Service + PostgreSQL + Outbox Pattern + Kafka já está sendo implementado. Os demais microsserviços serão adicionados incrementalmente.

📌 Objetivo

O objetivo deste projeto é aplicar, em um cenário próximo do mercado, os principais conceitos estudados de Kafka e sistemas distribuídos:

Producer e Consumer

Topics

Partitions

Consumer Groups

Kafka Keys

Offsets

Commit e AckMode

Retry e BackOff

Dead Letter Topic (DLT)

Idempotência

Event ID

PostgreSQL

Transações

Outbox Pattern

Comunicação assíncrona entre serviços

Docker

Observabilidade e rastreamento de eventos

🏗️ Arquitetura

Fluxo atual

                    Cliente
                       │
                       │ HTTP
                       ▼
                ┌──────────────┐
                │ Pedido       │
                │ Service      │
                └──────┬───────┘
                       │
                       │ @Transactional
                       ▼
                ┌──────────────┐
                │ PostgreSQL   │
                │              │
                │ pedidos      │
                │ outbox_event │
                └──────┬───────┘
                       │
                       │ Outbox Publisher
                       ▼
                ┌──────────────┐
                │    Kafka     │
                │   pedidos    │
                └──────────────┘

Arquitetura planejada

                         Cliente
                            │
                            │ REST
                            ▼
                    ┌───────────────┐
                    │ Pedido Service │
                    └───────┬───────┘
                            │
                       PostgreSQL
                            │
                          Outbox
                            │
                            ▼
                           Kafka
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
      ┌────────────┐ ┌────────────┐ ┌───────────────┐
      │ Pagamento  │ │  Estoque   │ │ Notificação   │
      │  Service   │ │  Service   │ │    Service    │
      └─────┬──────┘ └─────┬──────┘ └───────┬───────┘
            │                │                │
            ▼                ▼                ▼
         PostgreSQL       PostgreSQL       Serviço de
                                            notificação

🧰 Tecnologias

Java 21

Spring Boot 4.1.1

Spring Web

Spring Data JPA

Spring Kafka

PostgreSQL 16

Flyway

Apache Kafka 4.x

Docker / Docker Compose

Maven

📦 Serviços

Pedido Service

Responsável por:

receber pedidos via REST;

persistir o pedido;

gerar o eventId;

criar o evento PedidoCriado;

armazenar o evento na Outbox;

publicar posteriormente o evento no Kafka.

Fluxo atual:

POST /pedidos
↓
PedidoService
↓
PostgreSQL Transaction
├── Pedido
└── OutboxEvent
↓
OutboxPublisher
↓
Kafka

🔑 Conceitos importantes utilizados

Pedido ID

Identifica a entidade de negócio.

pedidoId = 100

Event ID

Identifica uma ocorrência específica de evento.

eventId = 550e8400-e29b-41d4-a716-446655440000

É utilizado para facilitar rastreamento e implementar idempotência nos consumidores.

Kafka Key

O pedidoId é utilizado como key:

key = pedidoId

Isso permite manter os eventos relacionados ao mesmo pedido na mesma partition, preservando a ordem dentro daquela partition.

Offset

Representa a posição de um registro dentro da partition.

Partition 2
────────────────────────
Offset 50 → Evento A
Offset 51 → Evento B
Offset 52 → Evento C

📨 Evento PedidoCriado

Exemplo:

{
"eventId": "abc-123",
"pedidoId": 1,
"tipo": "PEDIDO_CRIADO",
"cliente": "Alexandre",
"valor": 150.50
}

O evento é armazenado na Outbox como JSON antes de ser publicado no Kafka.

📤 Outbox Pattern

O projeto utiliza o Outbox Pattern para evitar o problema de salvar o pedido no banco e falhar ao publicar o evento.

Sem Outbox:

Salvar Pedido ✅
↓
Publicar Kafka ❌
↓
Pedido existe,
mas outros serviços não sabem dele.

Com Outbox:

BEGIN TRANSACTION
│
├── Salvar Pedido
│
└── Salvar OutboxEvent
│
COMMIT
│
▼
Outbox Publisher
│
▼
Kafka

Dessa forma, o pedido e o registro que representa o evento são persistidos na mesma transação do PostgreSQL.

🗃️ Estrutura da Outbox

Tabela:

outbox_event

Principais campos:

Campo

Descrição

id

Identificador interno

event_id

Identificador do evento

aggregate_id

ID da entidade relacionada

event_type

Tipo do evento

topic

Topic Kafka de destino

payload

Evento serializado em JSON

created_at

Data de criação

published_at

Data de publicação no Kafka

Enquanto:

published_at = NULL

o evento é considerado pendente de publicação.

🔄 Outbox Publisher

O publisher consulta periodicamente:

WHERE published_at IS NULL

e publica os eventos no Kafka.

Após a confirmação do envio:

Kafka confirmou ✅
↓
published_at atualizado

Se o Kafka estiver indisponível:

Kafka ❌
↓
published_at continua NULL
↓
próximo ciclo tenta novamente

🐳 Executando o projeto

1. Subir PostgreSQL e Kafka

Na raiz do projeto:

docker compose up -d

Verificar:

docker ps

Containers esperados:

ecommerce-postgres
ecommerce-kafka

2. Criar o tópico

docker exec -it ecommerce-kafka \
/opt/kafka/bin/kafka-topics.sh \
--create \
--topic pedidos \
--bootstrap-server localhost:9092 \
--partitions 3 \
--replication-factor 1

Listar tópicos:

docker exec -it ecommerce-kafka \
/opt/kafka/bin/kafka-topics.sh \
--list \
--bootstrap-server localhost:9092

3. Executar o Pedido Service

Linux/macOS:

./mvnw spring-boot:run

Windows:

.\mvnw.cmd spring-boot:run

🌐 API

Criar pedido

Endpoint

POST /pedidos

Request

{
"cliente": "Alexandre",
"valor": 150.50
}

Exemplo com cURL

curl -X POST http://localhost:8080/pedidos \
-H "Content-Type: application/json" \
-d "{\"cliente\":\"Alexandre\",\"valor\":150.50}"

Resposta esperada

{
"id": 1,
"cliente": "Alexandre",
"valor": 150.50,
"criadoEm": "2026-09-29T10:00:00"
}

🔎 Testando a Outbox

Acesse o PostgreSQL:

docker exec -it ecommerce-postgres \
psql -U postgres -d ecommerce

Consultar pedidos:

SELECT * FROM pedidos;

Consultar eventos:

SELECT
id,
event_id,
aggregate_id,
event_type,
topic,
published_at
FROM outbox_event
ORDER BY id DESC;

📡 Testando o Kafka

Consumir o tópico:

docker exec -it ecommerce-kafka \
/opt/kafka/bin/kafka-console-consumer.sh \
--topic pedidos \
--bootstrap-server localhost:9092 \
--from-beginning \
--property print.key=true \
--property key.separator=:

Exemplo:

1:{"eventId":"abc-123","pedidoId":1,"tipo":"PEDIDO_CRIADO","cliente":"Alexandre","valor":150.50}

🧪 Conceitos de confiabilidade estudados

O projeto também utiliza e/ou demonstrará os seguintes mecanismos:

Retry

Mensagem
↓
Erro
↓
Retry
↓
Retry
↓
Retry

DLT

Mensagem
↓
Retry
↓
Retry
↓
Retry
↓
pedidos.DLT

Idempotência

O consumidor poderá utilizar eventId para evitar efeitos duplicados caso o mesmo evento seja entregue novamente.

eventId = ABC

Primeira vez → processa ✅
Segunda vez  → já processado → ignora

🗂️ Estrutura planejada do repositório

ecommerce-kafka/
│
├── docker-compose.yml
│
├── pedido-service/
│   ├── src/
│   └── pom.xml
│
├── pagamento-service/
│   ├── src/
│   └── pom.xml
│
├── estoque-service/
│   ├── src/
│   └── pom.xml
│
├── notificacao-service/
│   ├── src/
│   └── pom.xml
│
└── README.md

🚧 Roadmap

✅ Concluído / em implementação

Configuração Docker

PostgreSQL

Kafka

Topic pedidos

Pedido Service

API POST /pedidos

Persistência de pedidos

Evento PedidoCriado

Outbox Pattern

Outbox Publisher

Publicação no Kafka

Retry

DLT

Idempotência

AckMode

🔜 Próximas etapas

Pagamento Service

Evento PagamentoAprovado

Evento PagamentoRecusado

Estoque Service

Evento EstoqueReservado

Evento EstoqueIndisponivel

Notificação Service

Consumer Groups independentes

Idempotência nos consumidores

Tratamento de DLT

Testes de integração

Testcontainers

Observabilidade

Correlation ID

Documentação da arquitetura

Melhorias para execução com múltiplas instâncias

📚 O que este projeto demonstra

Este projeto foi construído com foco no aprendizado prático de conceitos utilizados em arquiteturas distribuídas:

REST
↓
Spring Boot
↓
PostgreSQL
↓
Outbox Pattern
↓
Apache Kafka
↓
Consumer Groups
↓
Retry / DLT
↓
Idempotência
↓
Event-Driven Architecture

O objetivo não é apenas demonstrar um CRUD, mas mostrar como serviços independentes podem se comunicar por eventos de forma assíncrona e resiliente.

👨‍💻 Projeto de estudo e portfólio

Projeto desenvolvido para aprofundamento em:

Java

Spring Boot

Apache Kafka

PostgreSQL

Docker

Arquitetura de Microsserviços

Event-Driven Architecture

Sistemas distribuídos

O projeto continuará evoluindo conforme novos serviços e padrões forem implementados.