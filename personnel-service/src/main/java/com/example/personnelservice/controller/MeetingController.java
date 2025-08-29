package com.example.personnelservice.controller;

import com.example.personnelservice.model.Meeting;
import com.example.personnelservice.service.MeetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/meetings")
@Tag(name = "Meetings", description = "Manage meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private static final Logger log = LoggerFactory.getLogger(MeetingController.class);

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    @Operation(summary = "List all meetings")
    public ResponseEntity<List<Meeting>> list() {
        log.info("List meetings");
        meetingService.finalizePastMeetings();
        return ResponseEntity.ok(meetingService.listAll());
    }

    @GetMapping("/me")
    @Operation(summary = "List my meetings")
    public ResponseEntity<List<Meeting>> myMeetings(Authentication auth) {
        log.info("List my meetings for {}", auth != null ? auth.getPrincipal() : "unknown");
        meetingService.finalizePastMeetings();
        return ResponseEntity.ok(meetingService.listMine((String) auth.getPrincipal()));
    }

    @GetMapping("/user")
    @Operation(summary = "List meetings for current user")
    public ResponseEntity<List<Meeting>> userMeetings(Authentication auth) {
        log.info("List user meetings for {}", auth != null ? auth.getPrincipal() : "unknown");
        meetingService.finalizePastMeetings();
        return ResponseEntity.ok(meetingService.listUserMeetings((String) auth.getPrincipal()));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "List meetings by department")
    public ResponseEntity<List<Meeting>> byDepartment(@PathVariable Long departmentId) {
        log.info("List department meetings {}", departmentId);
        meetingService.finalizePastMeetings();
        return ResponseEntity.ok(meetingService.listByDepartment(departmentId));
    }

    @PostMapping("/create")
    @Operation(summary = "Create a meeting")
    public ResponseEntity<Meeting> create(@RequestBody CreateMeetingRequest req, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        try {
            log.info("Create meeting {} by {}", req.title, auth.getPrincipal());
            Meeting m = meetingService.create(req.title, req.description, req.departmentId, req.day, req.startTime, req.endTime, (String) auth.getPrincipal(), req.participantIds);
            return ResponseEntity.ok(m);
        } catch (IllegalArgumentException ex) {
            log.warn("Create meeting failed: {}", ex.getMessage());
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


