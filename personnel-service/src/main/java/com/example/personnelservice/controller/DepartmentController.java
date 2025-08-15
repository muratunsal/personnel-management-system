package com.example.personnelservice.controller;

import com.example.personnelservice.model.Department;
import com.example.personnelservice.repository.DepartmentRepository;
import com.example.personnelservice.repository.PersonRepository;
import com.example.personnelservice.model.Person;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public DepartmentController(DepartmentRepository departmentRepository, PersonRepository personRepository) {
        this.departmentRepository = departmentRepository;
        this.personRepository = personRepository;
    }

    @GetMapping
    public List<Department> all() {
        return departmentRepository.findAll();
    }

    @GetMapping("/organization-structure")
    public Map<String, Object> getOrganizationStructure() {
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
} 