package com.example.personnelservice.controller;

import com.example.personnelservice.dto.CreateTitleRequest;
import com.example.personnelservice.dto.UpdateTitleRequest;
import com.example.personnelservice.model.Department;
import com.example.personnelservice.model.Title;
import com.example.personnelservice.repository.DepartmentRepository;
import com.example.personnelservice.repository.TitleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/titles")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Titles", description = "Manage titles")
public class TitleController {
    private final TitleRepository titleRepository;
    private final DepartmentRepository departmentRepository;
    private final com.example.personnelservice.repository.PersonRepository personRepository;

    public TitleController(TitleRepository titleRepository, DepartmentRepository departmentRepository, com.example.personnelservice.repository.PersonRepository personRepository) {
        this.titleRepository = titleRepository;
        this.departmentRepository = departmentRepository;
        this.personRepository = personRepository;
    }

    @GetMapping
    @Operation(summary = "List all titles")
    public List<Title> all() {
        return titleRepository.findAll();
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "List titles by department")
    public List<Title> getTitlesByDepartment(@PathVariable Long departmentId) {
        return titleRepository.findByDepartmentId(departmentId);
    }

    @PostMapping
    @Operation(summary = "Create a title")
    public ResponseEntity<Title> createTitle(@Valid @RequestBody CreateTitleRequest request) {
        if (titleRepository.existsByNameIgnoreCase(request.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Title title = new Title();
        title.setName(request.getName());
        title.setDepartment(department);
        Title saved = titleRepository.save(title);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a title")
    public ResponseEntity<Title> updateTitle(@PathVariable Long id, @Valid @RequestBody UpdateTitleRequest request) {
        Title title = titleRepository.findById(id).orElse(null);
        if (title == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getName() != null) {
            title.setName(request.getName());
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            title.setDepartment(department);
        }

        Title saved = titleRepository.save(title);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a title and cleanup relations")
    public ResponseEntity<?> deleteTitle(@PathVariable Long id) {
        try {
            Title title = titleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Title not found"));

            java.util.List<com.example.personnelservice.model.Person> people = personRepository.findByTitleId(id);
            for (com.example.personnelservice.model.Person p : people) {
                p.setTitle(null);
                personRepository.save(p);
            }

            titleRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}