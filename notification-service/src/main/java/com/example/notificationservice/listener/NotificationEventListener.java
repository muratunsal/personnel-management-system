package com.example.notificationservice.listener;

import com.example.notificationservice.event.MeetingInvitationEvent;
import com.example.notificationservice.event.PersonUpdateEvent;
import com.example.notificationservice.event.UserProvisionedEvent;
import com.example.notificationservice.event.TaskAssignmentEvent;
import com.example.notificationservice.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "person.update.queue")
    public void handlePersonUpdate(PersonUpdateEvent event) {
        try {
            notificationService.handlePersonUpdate(event);
        } catch (Exception e) {
            
        }
    }

    @RabbitListener(queues = "task.assignment.queue")
    public void handleTaskAssignment(TaskAssignmentEvent event) {
        try {
            notificationService.handleTaskAssignment(event);
        } catch (Exception e) {
            
        }
    }

    @RabbitListener(queues = "meeting.invitation.queue")
    public void handleMeetingInvitation(MeetingInvitationEvent event) {
        try {
            notificationService.handleMeetingInvitation(event);
        } catch (Exception e) {
            
        }
    }

    @RabbitListener(queues = "user.provisioned.queue")
    public void handleUserProvisioned(UserProvisionedEvent event) {
        try {
            notificationService.handleUserProvisioned(event);
        } catch (Exception e) {
            
        }
    }
}
