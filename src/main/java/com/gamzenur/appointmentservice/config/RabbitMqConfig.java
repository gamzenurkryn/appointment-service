package com.gamzenur.appointmentservice.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConfig {

    public static final String APPOINTMENTS_EXCHANGE = "appointments";
    public static final String CREATED_ROUTING_KEY = "appointment.created";
    public static final String UPDATED_ROUTING_KEY = "appointment.updated";
    public static final String CANCELLED_ROUTING_KEY = "appointment.cancelled";
    public static final String CALLS_EXCHANGE = "calls";
    public static final String CALL_COMPLETED_ROUTING_KEY = "call.completed";
    public static final String APPOINTMENT_CALLS_QUEUE = "appointment.calls";

    @Bean
    public TopicExchange appointmentsExchange() {
        return new TopicExchange(APPOINTMENTS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange callsExchange() {
        return new TopicExchange(CALLS_EXCHANGE, true, false);
    }

    @Bean
    public Queue appointmentCallsQueue() {
        return new Queue(APPOINTMENT_CALLS_QUEUE, true);
    }

    @Bean
    public Binding appointmentCallCompletedBinding(
            Queue appointmentCallsQueue,
            TopicExchange callsExchange
    ) {
        return BindingBuilder.bind(appointmentCallsQueue)
                .to(callsExchange)
                .with(CALL_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        JsonMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rawMessageListenerContainerFactory(
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }
}
