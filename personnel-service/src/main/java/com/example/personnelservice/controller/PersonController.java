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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/people")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "People", description = "Manage people")
public class PersonController {

    private final PersonService personService;
    private static final Logger log = LoggerFactory.getLogger(PersonController.class);

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping
    @Operation(summary = "Create a person")
    public ResponseEntity<Person> createPerson(@Valid @RequestBody CreatePersonRequest request) {
        try {
            log.info("Create person request for {}", request.getEmail());
            Person createdPerson = personService.createPerson(request);
            log.info("Person created with id {}", createdPerson.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
        } catch (RuntimeException e) {
            log.warn("Create person failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "List people with filters and pagination")
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
            log.info("List people page {} size {} sort {} {}", page, size, sortBy, direction);
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
            
            log.info("People fetched count {}", people.getNumberOfElements());
            return ResponseEntity.ok(people);
        } catch (Exception e) {
            log.warn("List people failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error retrieving people: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get person by id")
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        log.info("Get person by id {}", id);
        return personService.findById(id)
                .map(person -> ResponseEntity.ok(person))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/test")
    @Operation(summary = "Health check for personnel service")
    public ResponseEntity<String> test() {
        log.info("Test endpoint called");
        return ResponseEntity.ok("Personnel service is working!");
    }

    @GetMapping("/test-auth")
    @Operation(summary = "Health check for auth integration")
    public ResponseEntity<String> testAuth() {
        log.info("Test-auth endpoint called");
        return ResponseEntity.ok("Authentication is working!");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a person")
    public ResponseEntity<?> updatePerson(@PathVariable Long id, @Valid @RequestBody UpdatePersonRequest request) {
        try {
            log.info("Update person {}", id);
            Person updatedPerson = personService.updatePerson(id, request);
            log.info("Person updated {}", updatedPerson.getId());
            return ResponseEntity.ok(updatedPerson);
        } catch (RuntimeException e) {
            log.warn("Update person failed {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("Failed to update person: " + e.getMessage());
        }
    }



    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a person and cleanup relations")
    public ResponseEntity<?> deletePerson(@PathVariable Long id) {
        try {
            log.info("Delete person {}", id);
            personService.deletePerson(id);
            log.info("Person deleted {}", id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("Delete person failed {}: {}", id, e.getMessage());
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