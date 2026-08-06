package com.gamzenur.crmservice.config;

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

    public static final String APPOINTMENTS_QUEUE = "crm.appointments";

    @Bean
    public TopicExchange appointmentsExchange() {
        return new TopicExchange("appointments", true, false);
    }

    @Bean
    public Queue crmAppointmentsQueue() {
        return new Queue(APPOINTMENTS_QUEUE, true);
    }

    @Bean
    public Binding crmAppointmentBinding(Queue crmAppointmentsQueue, TopicExchange appointmentsExchange) {
        return BindingBuilder.bind(crmAppointmentsQueue)
                .to(appointmentsExchange)
                .with("appointment.created");
    }
}
