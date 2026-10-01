package com.ecommerce.pagamento.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public DefaultErrorHandler paymentErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record,excption)-> new TopicPartition(record.topic()+".pagamento.DLT", record.partition()));
        recoverer.setFailIfSendResultIsError(true);

        return new DefaultErrorHandler(recoverer,
                new FixedBackOff(1000L,2));

    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,String>  kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String,String> factory =
                new ConcurrentKafkaListenerContainerFactory<String,String>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.RECORD
        );
        return factory;
    }
}
