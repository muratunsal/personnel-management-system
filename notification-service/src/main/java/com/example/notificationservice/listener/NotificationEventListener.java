package com.example.notificationservice.listener;

import com.example.notificationservice.event.MeetingInvitationEvent;
import com.example.notificationservice.event.PersonUpdateEvent;
import com.example.notificationservice.event.UserProvisionedEvent;
import com.example.notificationservice.event.TaskAssignmentEvent;
import com.example.notificationservice.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "person.update.queue")
    public void handlePersonUpdate(PersonUpdateEvent event) {
        try {
            log.info("Person update event received for {}", event.getPersonEmail());
            notificationService.handlePersonUpdate(event);
        } catch (Exception e) {
            log.warn("Failed to handle person update: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "task.assignment.queue")
    public void handleTaskAssignment(TaskAssignmentEvent event) {
        try {
            log.info("Task assignment event received for assignee {}", event.getAssigneeEmail());
            notificationService.handleTaskAssignment(event);
        } catch (Exception e) {
            log.warn("Failed to handle task assignment: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "meeting.invitation.queue")
    public void handleMeetingInvitation(MeetingInvitationEvent event) {
        try {
            log.info("Meeting invitation event received {}", event.getMeetingTitle());
            notificationService.handleMeetingInvitation(event);
        } catch (Exception e) {
            log.warn("Failed to handle meeting invitation: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "user.provisioned.queue")
    public void handleUserProvisioned(UserProvisionedEvent event) {
        try {
            log.info("User provisioned event received for {}", event.getEmail());
            notificationService.handleUserProvisioned(event);
        } catch (Exception e) {
            log.warn("Failed to handle user provisioned: {}", e.getMessage());
        }
    }
}
