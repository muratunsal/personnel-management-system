package com.example.personnelservice.service;

import com.example.personnelservice.model.Person;
import com.example.personnelservice.repository.PersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PersonService {
    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    

    public Page<Person> search(
            String query,
            String firstName,
            String lastName,
            String email,
            Long departmentId,
            Long titleId,
            String gender,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate birthDateFrom,
            LocalDate birthDateTo,
            Pageable pageable
    ) {
        List<Specification<Person>> filters = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            String like = "%" + query.toLowerCase() + "%";
            filters.add((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("phoneNumber")), like),
                    cb.like(cb.lower(root.get("address")), like)
            ));
        }
        if (firstName != null && !firstName.isBlank()) {
            String like = "%" + firstName.toLowerCase() + "%";
            filters.add((root, cq, cb) -> cb.like(cb.lower(root.get("firstName")), like));
        }
        if (lastName != null && !lastName.isBlank()) {
            String like = "%" + lastName.toLowerCase() + "%";
            filters.add((root, cq, cb) -> cb.like(cb.lower(root.get("lastName")), like));
        }
        if (email != null && !email.isBlank()) {
            String like = "%" + email.toLowerCase() + "%";
            filters.add((root, cq, cb) -> cb.like(cb.lower(root.get("email")), like));
        }
        if (departmentId != null) {
            filters.add((root, cq, cb) -> cb.equal(root.get("department").get("id"), departmentId));
        }
        if (titleId != null) {
            filters.add((root, cq, cb) -> cb.equal(root.get("title").get("id"), titleId));
        }
        if (gender != null && !gender.isBlank()) {
            filters.add((root, cq, cb) -> cb.equal(root.get("gender"), gender));
        }
        if (startDateFrom != null) {
            filters.add((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
        }
        if (startDateTo != null) {
            filters.add((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), startDateTo));
        }
        if (birthDateFrom != null) {
            filters.add((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("birthDate"), birthDateFrom));
        }
        if (birthDateTo != null) {
            filters.add((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("birthDate"), birthDateTo));
        }

        Specification<Person> spec = Specification.where(null);
        for (Specification<Person> f : filters) {
            spec = spec.and(f);
        }

        return personRepository.findAll(spec, pageable);
    }
} 