package com.example.personnelservice.repository;

import com.example.personnelservice.model.Meeting;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByParticipantsContainsOrderByDayAscStartTimeAsc(Person participant);
    List<Meeting> findByOrganizerOrderByDayAscStartTimeAsc(Person organizer);
    List<Meeting> findByDepartmentOrderByDayAscStartTimeAsc(Department department);
    List<Meeting> findByFinalizedFalseAndDayBefore(LocalDate threshold);
}


