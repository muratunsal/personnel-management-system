package com.example.personnelservice.service;

import com.example.personnelservice.model.Department;
import com.example.personnelservice.model.Meeting;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.repository.DepartmentRepository;
import com.example.personnelservice.repository.MeetingRepository;
import com.example.personnelservice.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;

    public MeetingService(MeetingRepository meetingRepository,
                          PersonRepository personRepository,
                          DepartmentRepository departmentRepository) {
        this.meetingRepository = meetingRepository;
        this.personRepository = personRepository;
        this.departmentRepository = departmentRepository;
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
        return meetingRepository.save(m);
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
}


