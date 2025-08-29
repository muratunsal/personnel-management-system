package com.example.personnelservice.service;

import com.example.personnelservice.model.Department;
import com.example.personnelservice.model.Meeting;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.repository.DepartmentRepository;
import com.example.personnelservice.repository.MeetingRepository;
import com.example.personnelservice.repository.PersonRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.personnelservice.config.RabbitMQConfig;
import com.example.personnelservice.event.MeetingInvitationEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;
    private final RabbitTemplate rabbitTemplate;

    public MeetingService(MeetingRepository meetingRepository,
                          PersonRepository personRepository,
                          DepartmentRepository departmentRepository,
                          RabbitTemplate rabbitTemplate) {
        this.meetingRepository = meetingRepository;
        this.personRepository = personRepository;
        this.departmentRepository = departmentRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Meeting.ComputedStatus computeStatus(Meeting m, LocalDateTime now) {
        if (m.isFinalized()) return Meeting.ComputedStatus.AFTER;
        LocalDate date = m.getDay();
        LocalDateTime start = LocalDateTime.of(date, m.getStartTime());
        LocalDateTime end = LocalDateTime.of(date, m.getEndTime());
        if (now.isBefore(start)) return Meeting.ComputedStatus.BEFORE;
        if (now.isAfter(end)) return Meeting.ComputedStatus.AFTER;
        return Meeting.ComputedStatus.ONGOING;
    }

    @Transactional
    public void finalizePastMeetings() {
        List<Meeting> past = meetingRepository.findByFinalizedFalseAndDayBefore(LocalDate.now());
        for (Meeting m : past) {
            m.setFinalized(true);
        }
        meetingRepository.saveAll(past);
    }

    @Transactional
    public Meeting create(String title, String description, Long departmentId, LocalDate day, LocalTime startTime, LocalTime endTime, String organizerEmail, List<Long> participantIds) {
        if (day == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end must be provided");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAt = LocalDateTime.of(day, startTime);
        LocalDateTime endAt = LocalDateTime.of(day, endTime);
        if (!startAt.isAfter(now)) {
            throw new IllegalArgumentException("Start must be after current time");
        }
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("End must be after start");
        }
        Person organizer = personRepository.findByEmail(organizerEmail);
        Meeting m = new Meeting();
        m.setTitle(title);
        m.setDescription(description);
        m.setOrganizer(organizer);
        if (departmentId != null) {
            Department dep = departmentRepository.findById(departmentId).orElse(null);
            m.setDepartment(dep);
        }
        m.setDay(day);
        m.setStartTime(startTime);
        m.setEndTime(endTime);
        Set<Person> participants = new HashSet<>();
        if (participantIds != null) {
            for (Long pid : participantIds) {
                personRepository.findById(pid).ifPresent(participants::add);
            }
        }
        m.setParticipants(participants);
        Meeting savedMeeting = meetingRepository.save(m);
        
        if (!participants.isEmpty()) {
            try {
                sendMeetingInvitationEvent(savedMeeting);
            } catch (Exception ignored) {
            }
        }
        
        return savedMeeting;
    }

    public List<Meeting> listAll() { return meetingRepository.findAll(); }

    public List<Meeting> listMine(String email) {
        Person me = personRepository.findByEmail(email);
        List<Meeting> asOrganizer = meetingRepository.findByOrganizerOrderByDayAscStartTimeAsc(me);
        List<Meeting> asParticipant = meetingRepository.findByParticipantsContainsOrderByDayAscStartTimeAsc(me);
        java.util.LinkedHashSet<Meeting> set = new java.util.LinkedHashSet<>();
        set.addAll(asOrganizer);
        set.addAll(asParticipant);
        return new java.util.ArrayList<>(set);
    }

    public List<Meeting> listUserMeetings(String email) {
        Person me = personRepository.findByEmail(email);
        List<Meeting> asOrganizer = meetingRepository.findByOrganizerOrderByDayAscStartTimeAsc(me);
        List<Meeting> asParticipant = meetingRepository.findByParticipantsContainsOrderByDayAscStartTimeAsc(me);
        java.util.LinkedHashSet<Meeting> set = new java.util.LinkedHashSet<>();
        set.addAll(asOrganizer);
        set.addAll(asParticipant);
        return new java.util.ArrayList<>(set);
    }

    public List<Meeting> listByDepartment(Long departmentId) {
        Department d = departmentRepository.findById(departmentId).orElseThrow();
        return meetingRepository.findByDepartmentOrderByDayAscStartTimeAsc(d);
    }

    private void sendMeetingInvitationEvent(Meeting meeting) {
        MeetingInvitationEvent event = new MeetingInvitationEvent();
        event.setMeetingId(meeting.getId());
        event.setMeetingTitle(meeting.getTitle());
        event.setMeetingDescription(meeting.getDescription());
        Person organizer = meeting.getOrganizer();
        if (organizer != null) {
            event.setOrganizerId(organizer.getId());
            event.setOrganizerEmail(organizer.getEmail());
            event.setOrganizerName(organizer.getFirstName() + " " + organizer.getLastName());
        } else {
            event.setOrganizerId(null);
            event.setOrganizerEmail(null);
            event.setOrganizerName("Admin");
        }
        event.setDepartmentName(meeting.getDepartment() != null ? meeting.getDepartment().getName() : "General");
        event.setMeetingDay(meeting.getDay());
        event.setStartTime(meeting.getStartTime());
        event.setEndTime(meeting.getEndTime());
        event.setParticipantIds(meeting.getParticipants().stream().map(Person::getId).collect(Collectors.toSet()));
        event.setParticipantEmails(meeting.getParticipants().stream().map(Person::getEmail).collect(Collectors.toSet()));
        event.setParticipantNames(meeting.getParticipants().stream().map(p -> p.getFirstName() + " " + p.getLastName()).collect(Collectors.toSet()));
        java.util.Map<String, String> emailToName = new java.util.HashMap<>();
        for (Person p : meeting.getParticipants()) {
            emailToName.put(p.getEmail(), p.getFirstName() + " " + p.getLastName());
        }
        if (organizer != null) {
            emailToName.put(organizer.getEmail(), organizer.getFirstName() + " " + organizer.getLastName());
        }
        event.setParticipantEmailToName(emailToName);
        event.setCreatedAt(meeting.getCreatedAt().toLocalDate());
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.MEETING_INVITATION_EXCHANGE, 
                                    RabbitMQConfig.MEETING_INVITATION_ROUTING_KEY, event);
    }
}


