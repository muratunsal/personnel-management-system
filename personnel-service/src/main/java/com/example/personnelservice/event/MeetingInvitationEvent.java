package com.example.personnelservice.event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.Map;

public class MeetingInvitationEvent {
    private Long meetingId;
    private String meetingTitle;
    private String meetingDescription;
    private Long organizerId;
    private String organizerEmail;
    private String organizerName;
    private String departmentName;
    private LocalDate meetingDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Set<Long> participantIds;
    private Set<String> participantEmails;
    private Set<String> participantNames;
    private LocalDate createdAt;
    private Map<String, String> participantEmailToName;

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    
    public String getMeetingTitle() { return meetingTitle; }
    public void setMeetingTitle(String meetingTitle) { this.meetingTitle = meetingTitle; }
    
    public String getMeetingDescription() { return meetingDescription; }
    public void setMeetingDescription(String meetingDescription) { this.meetingDescription = meetingDescription; }
    
    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
    
    public String getOrganizerEmail() { return organizerEmail; }
    public void setOrganizerEmail(String organizerEmail) { this.organizerEmail = organizerEmail; }
    
    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    
    public LocalDate getMeetingDay() { return meetingDay; }
    public void setMeetingDay(LocalDate meetingDay) { this.meetingDay = meetingDay; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Set<Long> getParticipantIds() { return participantIds; }
    public void setParticipantIds(Set<Long> participantIds) { this.participantIds = participantIds; }
    
    public Set<String> getParticipantEmails() { return participantEmails; }
    public void setParticipantEmails(Set<String> participantEmails) { this.participantEmails = participantEmails; }
    
    public Set<String> getParticipantNames() { return participantNames; }
    public void setParticipantNames(Set<String> participantNames) { this.participantNames = participantNames; }
    
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public Map<String, String> getParticipantEmailToName() { return participantEmailToName; }
    public void setParticipantEmailToName(Map<String, String> participantEmailToName) { this.participantEmailToName = participantEmailToName; }
}
