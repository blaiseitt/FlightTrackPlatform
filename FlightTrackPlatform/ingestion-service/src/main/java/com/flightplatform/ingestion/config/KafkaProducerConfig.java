package com.flightplatform.ingestion.config;

import com.flightplatform.common.event.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {
    @Bean
    public NewTopic flightPositionsTopic() {
        return TopicBuilder.name(KafkaTopics.FLIGHT_POSITIONS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic flightStatusTopic() {
        return TopicBuilder.name(KafkaTopics.FLIGHT_STATUS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
