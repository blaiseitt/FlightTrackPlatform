package com.flightplatform.weather.openweather;

import com.flightplatform.common.event.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public NewTopic weatherSnapshotsTopic() {
        return TopicBuilder.name(KafkaTopics.WEATHER_SNAPSHOTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
