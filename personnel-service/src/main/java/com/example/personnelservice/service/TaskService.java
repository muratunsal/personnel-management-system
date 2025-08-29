package com.example.personnelservice.service;

import com.example.personnelservice.model.Department;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.model.Task;
import com.example.personnelservice.repository.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.personnelservice.config.RabbitMQConfig;
import com.example.personnelservice.event.TaskAssignmentEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;
    private final RabbitTemplate rabbitTemplate;

    public TaskService(TaskRepository taskRepository,
                       PersonRepository personRepository,
                       DepartmentRepository departmentRepository,
                       RabbitTemplate rabbitTemplate) {
        this.taskRepository = taskRepository;
        this.personRepository = personRepository;
        this.departmentRepository = departmentRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Person getCurrentUser(String email) {
        return personRepository.findByEmail(email);
    }

    @Transactional
    public Task createTask(String title, String description, Task.Priority priority, Long departmentId, String creatorEmail) {
        Person creator = personRepository.findByEmail(creatorEmail);
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority != null ? priority : Task.Priority.MEDIUM);
        task.setCreatedBy(creator);
        if (departmentId != null) {
            Department dep = departmentRepository.findById(departmentId).orElse(null);
            task.setDepartment(dep);
        }
        return taskRepository.save(task);
    }

    @Transactional
    public Task createTaskWithAssignee(String title, String description, Task.Priority priority, Long departmentId, Long assigneeId, String creatorEmail) {
        Task task = createTask(title, description, priority, departmentId, creatorEmail);
        if (assigneeId != null) {
            Person assignee = personRepository.findById(assigneeId).orElseThrow();
            task.setAssignee(assignee);
        }
        Task savedTask = taskRepository.save(task);
        
        if (savedTask.getAssignee() != null) {
            try {
                sendTaskAssignmentEvent(savedTask);
            } catch (Exception e) {
                
            }
        }
        
        return savedTask;
    }

    public List<Task> getTasksForAssignee(String email) {
        Person p = personRepository.findByEmail(email);
        return taskRepository.findByAssigneeOrderByCreatedAtDesc(p);
    }

    public List<Task> getTasksForUser(String email) {
        Person p = personRepository.findByEmail(email);
        List<Task> createdTasks = taskRepository.findByCreatedByOrderByCreatedAtDesc(p);
        List<Task> assignedTasks = taskRepository.findByAssigneeOrderByCreatedAtDesc(p);
        
        java.util.LinkedHashSet<Task> allTasks = new java.util.LinkedHashSet<>();
        allTasks.addAll(createdTasks);
        allTasks.addAll(assignedTasks);
        
        return allTasks.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }


    @Transactional
    public Task updateTaskStatus(Long taskId, Task.Status status, String email) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        if (status == null) return task;
        if (status == Task.Status.IN_PROGRESS) {
            if (task.getStatus() != Task.Status.ASSIGNED) return task;
            if (task.getAssignee() == null || !task.getAssignee().getEmail().equals(email)) return task;
            task.setStatus(Task.Status.IN_PROGRESS);
        } else if (status == Task.Status.COMPLETED) {
            if (task.getStatus() != Task.Status.IN_PROGRESS) return task;
            if (task.getAssignee() == null || !task.getAssignee().getEmail().equals(email)) return task;
            task.setStatus(Task.Status.COMPLETED);
        }
        return taskRepository.save(task);
    }

    @Transactional
    public Task closeTask(Long taskId, String requesterEmail) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        if (task.getCreatedBy() != null && task.getCreatedBy().getEmail().equals(requesterEmail)) {
            if (task.getStatus() == Task.Status.COMPLETED) {
                task.setStatus(Task.Status.CLOSED);
                return taskRepository.save(task);
            }
        }
        return task;
    }

    public List<Task> listAll() {
        return taskRepository.findAll();
    }

    private void sendTaskAssignmentEvent(Task task) {
        TaskAssignmentEvent event = new TaskAssignmentEvent();
        event.setTaskId(task.getId());
        event.setTaskTitle(task.getTitle());
        event.setTaskDescription(task.getDescription());
        event.setPriority(task.getPriority().name());
        event.setAssigneeId(task.getAssignee().getId());
        event.setAssigneeEmail(task.getAssignee().getEmail());
        event.setAssigneeName(task.getAssignee().getFirstName() + " " + task.getAssignee().getLastName());
        Person creator = task.getCreatedBy();
        if (creator != null) {
            event.setAssignedBy(creator.getId());
            event.setAssignedByEmail(creator.getEmail());
            event.setAssignedByName(creator.getFirstName() + " " + creator.getLastName());
        } else {
            event.setAssignedBy(null);
            event.setAssignedByEmail(null);
            event.setAssignedByName("Admin");
        }
        event.setDepartmentName(task.getDepartment() != null ? task.getDepartment().getName() : "General");
        event.setAssignedAt(task.getCreatedAt());
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.TASK_ASSIGNMENT_EXCHANGE,
                RabbitMQConfig.TASK_ASSIGNMENT_ROUTING_KEY, event);
    }
}


