package com.example.personnelservice.controller;

import com.example.personnelservice.dto.CreatePersonRequest;
import com.example.personnelservice.dto.UpdatePersonRequest;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<Person> createPerson(@Valid @RequestBody CreatePersonRequest request) {
        try {
            Person createdPerson = personService.createPerson(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> getPeople(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            @RequestParam(value = "q", required = false) String searchQuery,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "titleId", required = false) Long titleId,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "contractStartDateFrom", required = false) String contractStartDateFromStr,
            @RequestParam(value = "contractStartDateTo", required = false) String contractStartDateToStr,
            @RequestParam(value = "birthDateFrom", required = false) String birthDateFromStr,
            @RequestParam(value = "birthDateTo", required = false) String birthDateToStr) {
        
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(direction.toUpperCase()), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            LocalDate contractStartDateFrom = parseDate(contractStartDateFromStr);
            LocalDate contractStartDateTo = parseDate(contractStartDateToStr);
            LocalDate birthDateFrom = parseDate(birthDateFromStr);
            LocalDate birthDateTo = parseDate(birthDateToStr);
            
            Page<Person> people = personService.findPeopleWithFilters(
                searchQuery, firstName, lastName, email, phoneNumber, departmentId, titleId, gender, address,
                contractStartDateFrom, contractStartDateTo, birthDateFrom, birthDateTo, pageable
            );
            
            return ResponseEntity.ok(people);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving people: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        return personService.findById(id)
                .map(person -> ResponseEntity.ok(person))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Personnel service is working!");
    }

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth() {
        return ResponseEntity.ok("Authentication is working!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePerson(@PathVariable Long id, @Valid @RequestBody UpdatePersonRequest request) {
        try {
            Person updatedPerson = personService.updatePerson(id, request);
            return ResponseEntity.ok(updatedPerson);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to update person: " + e.getMessage());
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePerson(@PathVariable Long id) {
        try {
            personService.deletePerson(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to delete person: " + e.getMessage());
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}