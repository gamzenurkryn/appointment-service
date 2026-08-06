package com.gamzenur.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "integration.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConfig {

    public static final String APPOINTMENTS_QUEUE = "notification.appointments";
    public static final String WHATSAPP_QUEUE = "notification.whatsapp";
    public static final String CALLS_QUEUE = "notification.calls";

    @Bean
    public TopicExchange appointmentsExchange() {
        return new TopicExchange("appointments", true, false);
    }

    @Bean
    public TopicExchange whatsappExchange() {
        return new TopicExchange("whatsapp", true, false);
    }

    @Bean
    public TopicExchange callsExchange() {
        return new TopicExchange("calls", true, false);
    }

    @Bean
    public Queue notificationAppointmentsQueue() {
        return new Queue(APPOINTMENTS_QUEUE, true);
    }

    @Bean
    public Queue notificationWhatsAppQueue() {
        return new Queue(WHATSAPP_QUEUE, true);
    }

    @Bean
    public Queue notificationCallsQueue() {
        return new Queue(CALLS_QUEUE, true);
    }

    @Bean
    public Binding appointmentBinding(Queue notificationAppointmentsQueue, TopicExchange appointmentsExchange) {
        return BindingBuilder.bind(notificationAppointmentsQueue).to(appointmentsExchange).with("appointment.*");
    }

    @Bean
    public Binding whatsappBinding(Queue notificationWhatsAppQueue, TopicExchange whatsappExchange) {
        return BindingBuilder.bind(notificationWhatsAppQueue).to(whatsappExchange).with("message.*");
    }

    @Bean
    public Binding callsBinding(Queue notificationCallsQueue, TopicExchange callsExchange) {
        return BindingBuilder.bind(notificationCallsQueue).to(callsExchange).with("call.*");
    }
}
