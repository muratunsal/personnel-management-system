package com.example.personnelservice.controller;

import com.example.personnelservice.dto.AssignHeadRequest;
import com.example.personnelservice.dto.CreateDepartmentRequest;
import com.example.personnelservice.dto.UpdateDepartmentRequest;
import com.example.personnelservice.model.Department;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.model.Title;
import com.example.personnelservice.repository.DepartmentRepository;
import com.example.personnelservice.repository.PersonRepository;
import com.example.personnelservice.repository.TitleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "http://localhost:3000")
public class DepartmentController {
    private final DepartmentRepository departmentRepository;
    private final PersonRepository personRepository;
    private final TitleRepository titleRepository;
    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    public DepartmentController(DepartmentRepository departmentRepository,
                                PersonRepository personRepository,
                                TitleRepository titleRepository) {
        this.departmentRepository = departmentRepository;
        this.personRepository = personRepository;
        this.titleRepository = titleRepository;
    }

    @GetMapping
    public List<Department> all() {
        log.info("List departments");
        return departmentRepository.findAll();
    }

    @GetMapping("/organization-structure")
    public Map<String, Object> getOrganizationStructure() {
        log.info("Get organization structure");
        List<Department> departments = departmentRepository.findAll();
        List<Person> people = personRepository.findAll();

        Map<Long, List<Person>> peopleByDepartment = new HashMap<>();
        for (Person person : people) {
            if (person.getDepartment() == null) {
                continue;
            }
            Long deptId = person.getDepartment().getId();
            List<Person> list = peopleByDepartment.get(deptId);
            if (list == null) {
                list = new ArrayList<>();
                peopleByDepartment.put(deptId, list);
            }
            list.add(person);
        }

        List<Map<String, Object>> departmentStructure = new ArrayList<>();
        for (Department dept : departments) {
            List<Person> deptPeople = peopleByDepartment.get(dept.getId());
            if (deptPeople == null) {
                deptPeople = new ArrayList<>();
            }

            List<Person> employees = new ArrayList<>();
            Person head = dept.getHeadOfDepartment();
            Long headId = head != null ? head.getId() : null;
            for (Person person : deptPeople) {
                if (headId != null && headId.equals(person.getId())) {
                    continue;
                }
                employees.add(person);
            }

            Map<String, Object> deptMap = new HashMap<>();
            deptMap.put("id", dept.getId());
            deptMap.put("name", dept.getName());
            deptMap.put("color", dept.getColor());
            deptMap.put("headOfDepartment", head);
            deptMap.put("employees", employees);
            departmentStructure.add(deptMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("departments", departmentStructure);
        return result;
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        log.info("Create department {}", request.getName());
        if (departmentRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Department already exists {}", request.getName());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        String color = normalizeHex(request.getColor());
        Department department = new Department();
        department.setName(request.getName());
        department.setColor(color);
        Department saved = departmentRepository.save(department);
        log.info("Department created {}", saved.getId());

        String headTitleName = "Head of " + saved.getName();
        boolean existsGlobally = titleRepository.existsByNameIgnoreCase(headTitleName);
        if (!existsGlobally) {
            titleRepository.findByDepartmentIdAndNameIgnoreCase(saved.getId(), headTitleName)
                    .orElseGet(() -> {
                        Title t = new Title();
                        t.setName(headTitleName);
                        t.setDepartment(saved);
                        return titleRepository.save(t);
                    });
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @Valid @RequestBody UpdateDepartmentRequest request) {
        log.info("Update department {}", id);
        Department department = departmentRepository.findById(id).orElse(null);
        if (department == null) {
            log.warn("Department not found {}", id);
            return ResponseEntity.notFound().build();
        }
        if (request.getName() != null) {
            department.setName(request.getName());
        }
        if (request.getColor() != null) {
            department.setColor(normalizeHex(request.getColor()));
        }
        Department saved = departmentRepository.save(department);
        log.info("Department updated {}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{departmentId}/assign-head")
    public ResponseEntity<Department> assignHead(@PathVariable Long departmentId,
                                                 @Valid @RequestBody AssignHeadRequest request) {
        log.info("Assign head {} -> {}", departmentId, request.getPersonId());
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        Person person = personRepository.findById(request.getPersonId())
                .orElseThrow(() -> new RuntimeException("Person not found"));

        department.setHeadOfDepartment(person);

        String headTitleName = "Head of " + department.getName();
        if (titleRepository.existsByNameIgnoreCase(headTitleName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Title headTitle = titleRepository.findByDepartmentIdAndNameIgnoreCase(department.getId(), headTitleName)
                .orElseGet(() -> {
                    Title t = new Title();
                    t.setName(headTitleName);
                    t.setDepartment(department);
                    return titleRepository.save(t);
                });

        person.setDepartment(department);
        person.setTitle(headTitle);
        personRepository.save(person);

        Department saved = departmentRepository.save(department);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{departmentId}/clear-head")
    public ResponseEntity<Department> clearHead(@PathVariable Long departmentId) {
        log.info("Clear head for department {}", departmentId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        department.setHeadOfDepartment(null);
        Department saved = departmentRepository.save(department);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            log.info("Delete department {}", id);
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Department not found"));

            department.setHeadOfDepartment(null);

            List<Person> people = personRepository.findByDepartmentId(id);
            for (Person p : people) {
                p.setDepartment(null);
                p.setTitle(null);
                personRepository.save(p);
            }

            List<Title> titles = titleRepository.findByDepartmentId(id);
            for (Title t : titles) {
                titleRepository.delete(t);
            }

            departmentRepository.deleteById(id);
            log.info("Department deleted {}", id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("Delete department failed {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String normalizeHex(String input) {
        if (input == null) {
            return "#999999";
        }
        String s = input.trim();
        if (!s.startsWith("#")) {
            s = "#" + s;
        }
        if (s.matches("#[0-9a-fA-F]{6}")) {
            return s.toUpperCase();
        }
        if (s.matches("#[0-9a-fA-F]{3}")) {
            char r = s.charAt(1);
            char g = s.charAt(2);
            char b = s.charAt(3);
            return ("#" + r + r + g + g + b + b).toUpperCase();
        }
        return "#999999";
    }
}