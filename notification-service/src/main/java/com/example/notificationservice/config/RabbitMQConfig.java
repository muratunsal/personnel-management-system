package com.example.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PERSON_UPDATE_EXCHANGE = "person.update.exchange";
    public static final String TASK_ASSIGNMENT_EXCHANGE = "task.assignment.exchange";
    public static final String MEETING_INVITATION_EXCHANGE = "meeting.invitation.exchange";
    public static final String USER_PROVISIONED_EXCHANGE = "user.provisioned.exchange";

    public static final String PERSON_UPDATE_QUEUE = "person.update.queue";
    public static final String TASK_ASSIGNMENT_QUEUE = "task.assignment.queue";
    public static final String MEETING_INVITATION_QUEUE = "meeting.invitation.queue";
    public static final String USER_PROVISIONED_QUEUE = "user.provisioned.queue";

    public static final String PERSON_UPDATE_ROUTING_KEY = "person.update";
    public static final String TASK_ASSIGNMENT_ROUTING_KEY = "task.assignment";
    public static final String MEETING_INVITATION_ROUTING_KEY = "meeting.invitation";
    public static final String USER_PROVISIONED_ROUTING_KEY = "user.provisioned";

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        java.util.Map<String, Class<?>> idClassMapping = new java.util.HashMap<>();
        idClassMapping.put("com.example.personnelservice.event.PersonUpdateEvent", com.example.notificationservice.event.PersonUpdateEvent.class);
        idClassMapping.put("com.example.personnelservice.event.TaskAssignmentEvent", com.example.notificationservice.event.TaskAssignmentEvent.class);
        idClassMapping.put("com.example.personnelservice.event.MeetingInvitationEvent", com.example.notificationservice.event.MeetingInvitationEvent.class);
        idClassMapping.put("com.example.authservice.event.UserProvisionedEvent", com.example.notificationservice.event.UserProvisionedEvent.class);
        classMapper.setIdClassMapping(idClassMapping);
        converter.setClassMapper(classMapper);
        return converter;
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
    public Queue personUpdateQueue() {
        return new Queue(PERSON_UPDATE_QUEUE, true);
    }

    @Bean
    public Queue taskAssignmentQueue() {
        return new Queue(TASK_ASSIGNMENT_QUEUE, true);
    }

    @Bean
    public Queue meetingInvitationQueue() {
        return new Queue(MEETING_INVITATION_QUEUE, true);
    }

    @Bean
    public Queue userProvisionedQueue() {
        return new Queue(USER_PROVISIONED_QUEUE, true);
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

    @Bean
    public DirectExchange userProvisionedExchange() {
        return new DirectExchange(USER_PROVISIONED_EXCHANGE);
    }

    @Bean
    public Binding personUpdateBinding() {
        return BindingBuilder.bind(personUpdateQueue())
                .to(personUpdateExchange())
                .with(PERSON_UPDATE_ROUTING_KEY);
    }

    @Bean
    public Binding taskAssignmentBinding() {
        return BindingBuilder.bind(taskAssignmentQueue())
                .to(taskAssignmentExchange())
                .with(TASK_ASSIGNMENT_ROUTING_KEY);
    }

    @Bean
    public Binding meetingInvitationBinding() {
        return BindingBuilder.bind(meetingInvitationQueue())
                .to(meetingInvitationExchange())
                .with(MEETING_INVITATION_ROUTING_KEY);
    }

    @Bean
    public Binding userProvisionedBinding() {
        return BindingBuilder.bind(userProvisionedQueue())
                .to(userProvisionedExchange())
                .with(USER_PROVISIONED_ROUTING_KEY);
    }
}
