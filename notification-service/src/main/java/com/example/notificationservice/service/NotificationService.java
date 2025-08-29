package com.example.notificationservice.service;

import com.example.notificationservice.event.MeetingInvitationEvent;
import com.example.notificationservice.event.PersonUpdateEvent;
import com.example.notificationservice.event.TaskAssignmentEvent;
import com.example.notificationservice.event.UserProvisionedEvent;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

@Service
public class NotificationService {

    private final MailService mailService;
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(MailService mailService) {
        this.mailService = mailService;
    }

    public void handlePersonUpdate(PersonUpdateEvent event) {
        log.info("Sending person update email to {}", event.getPersonEmail());
        Context context = new Context();
        context.setVariable("personName", event.getPersonName());
        java.util.List<java.util.Map<String, String>> prettyChanges = new java.util.ArrayList<>();
        if (event.getChanges() != null) {
            for (PersonUpdateEvent.ChangeDetail c : event.getChanges()) {
                java.util.Map<String, String> item = new java.util.HashMap<>();
                item.put("label", toPrettyFieldName(c.getFieldName()));
                item.put("old", formatValueForField(c.getFieldName(), c.getOldValue()));
                item.put("new", formatValueForField(c.getFieldName(), c.getNewValue()));
                prettyChanges.add(item);
            }
        }
        context.setVariable("changes", prettyChanges);
        context.setVariable("updatedByName", event.getUpdatedByName());
        context.setVariable("updatedAt", event.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

        String subject = "Profile Update Notification";
        mailService.sendHtmlMail(event.getPersonEmail(), subject, "person-update", context);
        log.debug("Person update email sent to {}", event.getPersonEmail());
    }

    private String toPrettyFieldName(String key) {
        if (key == null || key.isBlank()) return "";
        switch (key) {
            case "firstName": return "First Name";
            case "lastName": return "Last Name";
            case "email": return "Email";
            case "phoneNumber": return "Phone Number";
            case "salary": return "Salary";
            case "address": return "Address";
            case "departmentId": return "Department";
            case "titleId": return "Title";
            case "profilePictureUrl": return "Profile Picture";
            case "gender": return "Gender";
            case "birthDate": return "Birth Date";
            case "contractStartDate": return "Contract Start Date";
            case "contractEndDate": return "Contract End Date";
            case "contractType": return "Contract Type";
            case "nationalId": return "National ID";
            case "bankAccount": return "Bank Account";
            case "insuranceNumber": return "Insurance Number";
            default:
                String withSpaces = key.replaceAll("([a-z])([A-Z])", "$1 $2");
                String cap = withSpaces.substring(0,1).toUpperCase() + withSpaces.substring(1);
                return cap;
        }
    }

    private String formatValueForField(String field, String raw) {
        if (raw == null || raw.isBlank()) return "Not provided";
        if ("contractStartDate".equals(field) || "contractEndDate".equals(field) || "birthDate".equals(field)) {
            if ("null".equals(raw)) return "Not provided";
            return raw;
        }
        if ("salary".equals(field)) {
            if ("null".equals(raw)) return "Not provided";
            try {
                Integer salary = Integer.parseInt(raw);
                return "$" + String.format("%,d", salary);
            } catch (Exception e) {
                return raw;
            }
        }
        return raw;
    }

    public void handleTaskAssignment(TaskAssignmentEvent event) {
        log.info("Sending task assignment email to {}", event.getAssigneeEmail());
        Context context = new Context();
        context.setVariable("assigneeName", event.getAssigneeName());
        context.setVariable("taskTitle", event.getTaskTitle());
        context.setVariable("taskDescription", event.getTaskDescription());
        context.setVariable("priority", event.getPriority());
        context.setVariable("assignedByName", event.getAssignedByName());
        context.setVariable("departmentName", event.getDepartmentName());
        context.setVariable("assignedAt", event.getAssignedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        

        String subject = "New Task Assignment: " + event.getTaskTitle();
        mailService.sendHtmlMail(event.getAssigneeEmail(), subject, "task-assignment", context);
        log.debug("Task assignment email sent to {}", event.getAssigneeEmail());
    }

    public void handleMeetingInvitation(MeetingInvitationEvent event) {
        log.info("Sending meeting invitation emails for {}", event.getMeetingTitle());
        Context context = new Context();
        context.setVariable("meetingTitle", event.getMeetingTitle());
        context.setVariable("meetingDescription", event.getMeetingDescription());
        context.setVariable("organizerName", (event.getOrganizerName() == null || event.getOrganizerName().isBlank()) ? "Admin" : event.getOrganizerName());
        context.setVariable("departmentName", event.getDepartmentName());
        context.setVariable("meetingDay", event.getMeetingDay().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        context.setVariable("startTime", event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        context.setVariable("endTime", event.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        String participantsList = String.join(", ", event.getParticipantNames());
        context.setVariable("participantNames", participantsList);

        String subject = "Meeting Invitation: " + event.getMeetingTitle();
        
        List<String> allEmails = new ArrayList<>();
        if (event.getParticipantEmails() != null) {
            for (String email : event.getParticipantEmails()) {
                if (email != null && !email.isBlank()) {
                    allEmails.add(email);
                }
            }
        }

        java.util.Set<String> uniqueEmails = new java.util.LinkedHashSet<>(allEmails);
        int sentCount = 0;
        for (String to : uniqueEmails) {
            String recipientName = event.getParticipantEmailToName() != null ? event.getParticipantEmailToName().getOrDefault(to, "Participant") : "Participant";
            Context personal = new Context();
            personal.setVariables(context.getVariableNames().stream().collect(java.util.stream.Collectors.toMap(n -> n, context::getVariable)));
            personal.setVariable("recipientName", recipientName);
            mailService.sendHtmlMail(to, subject, "meeting-invitation", personal);
            sentCount++;
        }
        log.debug("Meeting invitation emails sent count {}", sentCount);
    }

    public void handleUserProvisioned(UserProvisionedEvent event) {
        log.info("Sending user provisioned email to {}", event.getEmail());
        Context context = new Context();
        String recipientName = (event.getFullName() == null || event.getFullName().isBlank()) ? "User" : event.getFullName();
        context.setVariable("recipientName", recipientName);
        context.setVariable("password", event.getPassword());
        String subject = "Your Account Has Been Created";
        mailService.sendHtmlMail(event.getEmail(), subject, "user-provisioned", context);
        log.debug("User provisioned email sent to {}", event.getEmail());
    }
}
