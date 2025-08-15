package com.example.personnelservice.controller;

import com.example.personnelservice.model.Person;
import com.example.personnelservice.service.PersonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/people")
@CrossOrigin(origins = "http://localhost:3000")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public Page<Person> search(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "titleId", required = false) Long titleId,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "startDateFrom", required = false) String startDateFromStr,
            @RequestParam(value = "startDateTo", required = false) String startDateToStr,
            @RequestParam(value = "birthDateFrom", required = false) String birthDateFromStr,
            @RequestParam(value = "birthDateTo", required = false) String birthDateToStr
    ) {
        Sort.Direction sortDirection = Sort.Direction.ASC;
        if (!"asc".equalsIgnoreCase(direction)) {
            sortDirection = Sort.Direction.DESC;
        }
        Sort sort = Sort.by(sortDirection, sortBy);
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        LocalDate startDateFrom = parseDate(startDateFromStr);
        LocalDate startDateTo = parseDate(startDateToStr);
        LocalDate birthDateFrom = parseDate(birthDateFromStr);
        LocalDate birthDateTo = parseDate(birthDateToStr);

        return personService.search(query, firstName, lastName, email, departmentId, titleId, gender,
                startDateFrom, startDateTo, birthDateFrom, birthDateTo, pageable);
    }

    private LocalDate parseDate(String value) {
        try {
            return (value == null || value.isBlank()) ? null : LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
} 