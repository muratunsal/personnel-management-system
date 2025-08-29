package com.example.personnelservice.controller;

import com.example.personnelservice.model.Meeting;
import com.example.personnelservice.service.MeetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    public ResponseEntity<List<Meeting>> list() {
        meetingService.finalizePastMeetings();
        return ResponseEntity.ok(meetingService.listAll());
    }

    @GetMapping("/me")
    public ResponseEntity<List<Meeting>> myMeetings(Authentication auth) {
        meetingService.finalizePastMeetings();
        return ResponseEntity.ok(meetingService.listMine((String) auth.getPrincipal()));
    }

    @GetMapping("/user")
    public ResponseEntity<List<Meeting>> userMeetings(Authentication auth) {
        meetingService.finalizePastMeetings();
        return ResponseEntity.ok(meetingService.listUserMeetings((String) auth.getPrincipal()));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Meeting>> byDepartment(@PathVariable Long departmentId) {
        meetingService.finalizePastMeetings();
        return ResponseEntity.ok(meetingService.listByDepartment(departmentId));
    }

    @PostMapping("/create")
    public ResponseEntity<Meeting> create(@RequestBody CreateMeetingRequest req, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        try {
            Meeting m = meetingService.create(req.title, req.description, req.departmentId, req.day, req.startTime, req.endTime, (String) auth.getPrincipal(), req.participantIds);
            return ResponseEntity.ok(m);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    public static class CreateMeetingRequest {
        public String title;
        public String description;
        public Long departmentId;
        public LocalDate day;
        public LocalTime startTime;
        public LocalTime endTime;
        public List<Long> participantIds;
    }
}


