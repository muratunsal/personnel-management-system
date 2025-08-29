package com.example.personnelservice.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PERSON_UPDATE_EXCHANGE = "person.update.exchange";
    public static final String TASK_ASSIGNMENT_EXCHANGE = "task.assignment.exchange";
    public static final String MEETING_INVITATION_EXCHANGE = "meeting.invitation.exchange";

    public static final String PERSON_UPDATE_ROUTING_KEY = "person.update";
    public static final String TASK_ASSIGNMENT_ROUTING_KEY = "task.assignment";
    public static final String MEETING_INVITATION_ROUTING_KEY = "meeting.invitation";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public DirectExchange personUpdateExchange() {
        return new DirectExchange(PERSON_UPDATE_EXCHANGE);
    }

    @Bean
    public DirectExchange taskAssignmentExchange() {
        return new DirectExchange(TASK_ASSIGNMENT_EXCHANGE);
    }

    @Bean
    public DirectExchange meetingInvitationExchange() {
        return new DirectExchange(MEETING_INVITATION_EXCHANGE);
    }
}
