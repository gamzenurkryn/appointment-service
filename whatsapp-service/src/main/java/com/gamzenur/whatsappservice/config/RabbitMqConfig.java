package com.gamzenur.whatsappservice.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConfig {
    public static final String WHATSAPP_EXCHANGE = "whatsapp";
    public static final String MESSAGE_RECEIVED_ROUTING_KEY = "message.received";

    @Bean
    public TopicExchange whatsappExchange() {
        return new TopicExchange(WHATSAPP_EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        JsonMapper mapper = JsonMapper.builder().findAndAddModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
        return new Jackson2JsonMessageConverter(mapper);
    }
}
