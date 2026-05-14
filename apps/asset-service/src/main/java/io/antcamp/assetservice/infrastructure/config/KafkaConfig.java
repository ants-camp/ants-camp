package io.antcamp.assetservice.infrastructure.config;

import io.antcamp.assetservice.domain.event.payload.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private Map<String, Object> baseConsumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CompetitionRegisteredEvent> competitionRegisteredFactory() {
        Map<String, Object> props = baseConsumerConfig();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CompetitionRegisteredEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        ConsumerFactory<String, CompetitionRegisteredEvent> factory = new DefaultKafkaConsumerFactory<>(props);
        return new ConcurrentKafkaListenerContainerFactory<>() {{
            setConsumerFactory(factory);
        }};
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CompetitionEndedEvent> competitionEndedFactory() {
        Map<String, Object> props = baseConsumerConfig();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CompetitionEndedEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        ConsumerFactory<String, CompetitionEndedEvent> factory = new DefaultKafkaConsumerFactory<>(props);
        return new ConcurrentKafkaListenerContainerFactory<>() {{
            setConsumerFactory(factory);
        }};
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CompetitionTicked> competitionTickedFactory() {
        Map<String, Object> props = baseConsumerConfig();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CompetitionTicked.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        ConsumerFactory<String, CompetitionTicked> factory = new DefaultKafkaConsumerFactory<>(props);
        return new ConcurrentKafkaListenerContainerFactory<>() {{
            setConsumerFactory(factory);
        }};
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CompetitionCancelledEvent> competitionCancelledFactory() {
        Map<String, Object> props = baseConsumerConfig();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CompetitionCancelledEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        ConsumerFactory<String, CompetitionCancelledEvent> factory = new DefaultKafkaConsumerFactory<>(props);
        return new ConcurrentKafkaListenerContainerFactory<>() {{
            setConsumerFactory(factory);
        }};
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CompetitionAbortedEvent> competitionAbortedFactory() {
        Map<String, Object> props = baseConsumerConfig();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CompetitionAbortedEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        ConsumerFactory<String, CompetitionAbortedEvent> factory = new DefaultKafkaConsumerFactory<>(props);
        return new ConcurrentKafkaListenerContainerFactory<>() {{
            setConsumerFactory(factory);
        }};
    }
}