package com.example.personnelservice.controller;

import com.example.personnelservice.model.*;
import com.example.personnelservice.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<java.util.List<Task>> list() {
        log.info("List tasks");
        return ResponseEntity.ok(taskService.listAll());
    }

    @PostMapping("/create")
    public ResponseEntity<Task> createAndAssign(@RequestBody CreateAndAssignRequest req, Authentication auth) {
        if (!hasAnyRole(auth, "ROLE_ADMIN", "ROLE_HEAD", "ROLE_HR")) return ResponseEntity.status(403).build();
        log.info("Create task {} by {} for assignee {}", req.title, auth != null ? auth.getPrincipal() : "unknown", req.assigneeId);
        Task t = taskService.createTaskWithAssignee(req.title, req.description, req.priority, req.departmentId, req.assigneeId, (String) auth.getPrincipal());
        return ResponseEntity.ok(t);
    }

    @GetMapping("/me")
    public ResponseEntity<List<Task>> myTasks(Authentication auth) {
        log.info("List my tasks for {}", auth != null ? auth.getPrincipal() : "unknown");
        return ResponseEntity.ok(taskService.getTasksForAssignee((String) auth.getPrincipal()));
    }

    @GetMapping("/user")
    public ResponseEntity<List<Task>> userTasks(Authentication auth) {
        log.info("List user tasks for {}", auth != null ? auth.getPrincipal() : "unknown");
        return ResponseEntity.ok(taskService.getTasksForUser((String) auth.getPrincipal()));
    }


    @PutMapping("/{taskId}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable Long taskId, @RequestBody UpdateTaskStatusRequest req, Authentication auth) {
        if (!hasAnyRole(auth, "ROLE_ADMIN", "ROLE_HEAD", "ROLE_EMPLOYEE")) return ResponseEntity.status(403).build();
        log.info("Update task status {} -> {} by {}", taskId, req.status, auth != null ? auth.getPrincipal() : "unknown");
        Task t = taskService.updateTaskStatus(taskId, req.status, (String) auth.getPrincipal());
        return ResponseEntity.ok(t);
    }

    @PutMapping("/{taskId}/close")
    public ResponseEntity<Task> close(@PathVariable Long taskId, Authentication auth) {
        if (!hasAnyRole(auth, "ROLE_ADMIN", "ROLE_HEAD")) return ResponseEntity.status(403).build();
        log.info("Close task {} by {}", taskId, auth != null ? auth.getPrincipal() : "unknown");
        return ResponseEntity.ok(taskService.closeTask(taskId, (String) auth.getPrincipal()));
    }

    public static class CreateAndAssignRequest {
        public String title;
        public String description;
        public Task.Priority priority;
        public Long departmentId;
        public Long assigneeId;
    }

    public static class UpdateTaskStatusRequest {
        public Task.Status status;
    }

    private boolean hasAnyRole(Authentication auth, String... roles) {
        if (auth == null) return false;
        java.util.Set<String> owned = new java.util.HashSet<>();
        for (var a : auth.getAuthorities()) owned.add(a.getAuthority());
        for (String r : roles) if (owned.contains(r)) return true;
        return false;
    }
}


