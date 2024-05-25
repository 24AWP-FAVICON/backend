package com.example.demo.config;

import com.example.demo.event.AlarmMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap.servers.config}")
    private String bootstrapServersConfig;

    @Value("${spring.kafka.group.id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, AlarmMessage> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        JsonDeserializer<AlarmMessage> deserializer = new JsonDeserializer<>(AlarmMessage.class);
        deserializer.addTrustedPackages("kea.memberservice.entity");

        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AlarmMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AlarmMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}