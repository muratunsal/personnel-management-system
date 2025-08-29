package com.example.personnelservice.service;

import com.example.personnelservice.dto.CreatePersonRequest;
import com.example.personnelservice.dto.UpdatePersonRequest;
import com.example.personnelservice.model.Department;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.model.Title;
import com.example.personnelservice.repository.DepartmentRepository;
import com.example.personnelservice.repository.PersonRepository;
import com.example.personnelservice.repository.TitleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional
public class PersonService {

    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;
    private final TitleRepository titleRepository;

    private final com.example.personnelservice.service.AuthService authService;

    public PersonService(PersonRepository personRepository, 
                        DepartmentRepository departmentRepository, 
                        TitleRepository titleRepository,
                        com.example.personnelservice.service.AuthService authService) {
        this.personRepository = personRepository;
        this.departmentRepository = departmentRepository;
        this.titleRepository = titleRepository;
        this.authService = authService;
    }

    public Person createPerson(CreatePersonRequest request) {
        Person person = new Person();
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        person.setEmail(request.getEmail());
        person.setPhoneNumber(request.getPhoneNumber());
        person.setContractStartDate(request.getContractStartDate());
        person.setBirthDate(request.getBirthDate());
        person.setGender(request.getGender());
        person.setAddress(request.getAddress());
        person.setProfilePictureUrl(request.getProfilePictureUrl());
        
        person.setSalary(request.getSalary());
        person.setNationalId(request.getNationalId());
        person.setBankAccount(request.getBankAccount());
        person.setInsuranceNumber(request.getInsuranceNumber());
        person.setContractType(request.getContractType());
        person.setContractEndDate(request.getContractEndDate());

        Department targetDepartment = null;
        if (request.getDepartmentId() != null) {
            targetDepartment = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with ID: " + request.getDepartmentId()));
            person.setDepartment(targetDepartment);
        }

        Title targetTitle = null;
        if (request.getTitleId() != null) {
            targetTitle = titleRepository.findById(request.getTitleId())
                    .orElseThrow(() -> new RuntimeException("Title not found with ID: " + request.getTitleId()));
            person.setTitle(targetTitle);
        }

        if (targetDepartment != null && targetTitle != null) {
            String expectedHeadTitle = "Head of " + targetDepartment.getName();
            if (expectedHeadTitle.equalsIgnoreCase(targetTitle.getName())) {
                if (targetDepartment.getHeadOfDepartment() != null) {
                    throw new RuntimeException("This department already has a head");
                }
            }
        }

        Person saved = personRepository.save(person);

        if (targetDepartment != null && targetTitle != null) {
            java.util.List<String> roles = new java.util.ArrayList<>();
            boolean isHead = ("Head of " + targetDepartment.getName()).equalsIgnoreCase(targetTitle.getName());
            boolean isHr = "HR".equalsIgnoreCase(targetDepartment.getName());
            if (isHr) roles.add("HR");
            if (isHead) roles.add("HEAD");
            if (roles.isEmpty()) roles.add("EMPLOYEE");
            authService.provision(saved.getEmail(), roles);
        }

        if (targetDepartment != null && targetTitle != null) {
            String expectedHeadTitle = "Head of " + targetDepartment.getName();
            if (expectedHeadTitle.equalsIgnoreCase(targetTitle.getName())) {
                targetDepartment.setHeadOfDepartment(saved);
                departmentRepository.save(targetDepartment);
            }
        }

        return saved;
    }

    public Page<Person> findPeopleWithFilters(
            String searchQuery, String firstName, String lastName, String email, String phoneNumber,
            Long departmentId, Long titleId, String gender, String address,
            LocalDate contractStartDateFrom, LocalDate contractStartDateTo,
            LocalDate birthDateFrom, LocalDate birthDateTo, Pageable pageable) {
        
        return personRepository.findAll((root, cq, cb) -> {
            List<Predicate> filters = new ArrayList<>();
            
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String searchLower = searchQuery.toLowerCase();
                filters.add(cb.or(
                    cb.like(cb.lower(root.get("firstName")), "%" + searchLower + "%"),
                    cb.like(cb.lower(root.get("lastName")), "%" + searchLower + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + searchLower + "%"),
                    cb.like(cb.lower(root.get("phoneNumber")), "%" + searchLower + "%")
                ));
            }
            
            if (firstName != null && !firstName.trim().isEmpty()) {
                filters.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                filters.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }
            if (email != null && !email.trim().isEmpty()) {
                filters.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                filters.add(cb.like(cb.lower(root.get("phoneNumber")), "%" + phoneNumber.toLowerCase() + "%"));
            }
            if (departmentId != null) {
                filters.add(cb.equal(root.get("department").get("id"), departmentId));
            }
            if (titleId != null) {
                filters.add(cb.equal(root.get("title").get("id"), titleId));
            }
            if (gender != null && !gender.trim().isEmpty()) {
                filters.add(cb.equal(root.get("gender"), gender));
            }
            if (address != null && !address.trim().isEmpty()) {
                filters.add(cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase() + "%"));
            }
            if (contractStartDateFrom != null) {
                filters.add(cb.greaterThanOrEqualTo(root.get("contractStartDate"), contractStartDateFrom));
            }
            if (birthDateFrom != null) {
                filters.add(cb.greaterThanOrEqualTo(root.get("birthDate"), birthDateFrom));
            }
            if (contractStartDateTo != null) {
                filters.add(cb.lessThanOrEqualTo(root.get("contractStartDate"), contractStartDateTo));
            }
            if (birthDateTo != null) {
                filters.add(cb.lessThanOrEqualTo(root.get("birthDate"), birthDateTo));
            }
            
            return cb.and(filters.toArray(new Predicate[0]));
        }, pageable);
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }

    public Person updatePerson(Long id, UpdatePersonRequest request) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with ID: " + id));

        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        Long oldDepartmentId = person.getDepartment() != null ? person.getDepartment().getId() : null;
        Long oldTitleId = person.getTitle() != null ? person.getTitle().getId() : null;
        String oldEmail = person.getEmail();
        person.setEmail(request.getEmail());
        person.setPhoneNumber(request.getPhoneNumber());
        person.setContractStartDate(request.getContractStartDate());
        person.setBirthDate(request.getBirthDate());
        person.setGender(request.getGender());
        person.setAddress(request.getAddress());
        person.setProfilePictureUrl(request.getProfilePictureUrl());
        
        person.setSalary(request.getSalary() != null ? request.getSalary() : person.getSalary());
        person.setNationalId(request.getNationalId());
        person.setBankAccount(request.getBankAccount());
        person.setInsuranceNumber(request.getInsuranceNumber());
        person.setContractType(request.getContractType());
        person.setContractEndDate(request.getContractEndDate());

        Department targetDepartment = null;
        if (request.getDepartmentId() != null && request.getDepartmentId() > 0) {
            targetDepartment = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with ID: " + request.getDepartmentId()));
            person.setDepartment(targetDepartment);
        } else {
            person.setDepartment(null);
        }

        Title targetTitle = null;
        if (request.getTitleId() != null && request.getTitleId() > 0) {
            targetTitle = titleRepository.findById(request.getTitleId())
                    .orElseThrow(() -> new RuntimeException("Title not found with ID: " + request.getTitleId()));
            person.setTitle(targetTitle);
        } else {
            person.setTitle(null);
        }

        if (targetDepartment != null && targetTitle != null) {
            String expectedHeadTitle = "Head of " + targetDepartment.getName();
            if (expectedHeadTitle.equalsIgnoreCase(targetTitle.getName())) {
                if (targetDepartment.getHeadOfDepartment() != null &&
                        !targetDepartment.getHeadOfDepartment().getId().equals(person.getId())) {
                    throw new RuntimeException("This department already has a head");
                }
            }
        }

        Person savedPerson = personRepository.save(person);

        boolean deptChanged = (oldDepartmentId == null ? person.getDepartment() != null : (person.getDepartment() == null || !oldDepartmentId.equals(person.getDepartment().getId())));
        boolean titleChanged = (oldTitleId == null ? person.getTitle() != null : (person.getTitle() == null || !oldTitleId.equals(person.getTitle().getId())));

        boolean roleAffectingChange = deptChanged || titleChanged;

        if (targetDepartment != null && targetTitle != null && roleAffectingChange) {
            java.util.List<String> roles = new java.util.ArrayList<>();
            String expectedHeadTitle = "Head of " + targetDepartment.getName();
            boolean isHeadTitle = expectedHeadTitle.equalsIgnoreCase(targetTitle.getName());
            boolean isHr = "HR".equalsIgnoreCase(targetDepartment.getName());
            if (isHr) roles.add("HR");
            if (isHeadTitle) roles.add("HEAD");
            if (roles.isEmpty()) roles.add("EMPLOYEE");
            boolean updated = authService.updateUser(oldEmail, savedPerson.getEmail(), roles);
            if (!updated) {
                authService.provision(savedPerson.getEmail(), roles);
            }
        } else if ((oldDepartmentId == null || oldTitleId == null) && (targetDepartment != null && targetTitle != null)) {
            java.util.List<String> roles = new java.util.ArrayList<>();
            String expectedHeadTitle = "Head of " + targetDepartment.getName();
            boolean isHeadTitle = expectedHeadTitle.equalsIgnoreCase(targetTitle.getName());
            boolean isHr = "HR".equalsIgnoreCase(targetDepartment.getName());
            if (isHr) roles.add("HR");
            if (isHeadTitle) roles.add("HEAD");
            if (roles.isEmpty()) roles.add("EMPLOYEE");
            authService.provision(savedPerson.getEmail(), roles);
        } else if (!oldEmail.equalsIgnoreCase(savedPerson.getEmail())) {
            authService.updateUser(oldEmail, savedPerson.getEmail(), null);
        }
        
        if (savedPerson.getDepartment() != null && savedPerson.getTitle() != null) {
            java.util.List<String> roles = new java.util.ArrayList<>();
            boolean isHead = savedPerson.getTitle().getName().toLowerCase().startsWith("head of");
            boolean isHr = "HR".equalsIgnoreCase(savedPerson.getDepartment().getName());
            if (isHr) roles.add("HR");
            if (isHead) roles.add("HEAD");
            if (roles.isEmpty()) roles.add("EMPLOYEE");
            authService.provision(savedPerson.getEmail(), roles);
        }

        if (targetDepartment != null) {
            String expectedHeadTitle = targetTitle != null ? ("Head of " + targetDepartment.getName()) : null;
            boolean isHeadTitle = expectedHeadTitle != null && expectedHeadTitle.equalsIgnoreCase(targetTitle.getName());

            if (isHeadTitle) {
                targetDepartment.setHeadOfDepartment(savedPerson);
                departmentRepository.save(targetDepartment);
            } else {
                if (targetDepartment.getHeadOfDepartment() != null &&
                        targetDepartment.getHeadOfDepartment().getId().equals(savedPerson.getId())) {
                    targetDepartment.setHeadOfDepartment(null);
                    departmentRepository.save(targetDepartment);
                }
            }
        }

        return savedPerson;
    }

    public void deletePerson(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with ID: " + id));

        java.util.List<com.example.personnelservice.model.Department> departments = departmentRepository.findByHeadOfDepartmentId(id);
        for (com.example.personnelservice.model.Department dept : departments) {
            dept.setHeadOfDepartment(null);
        }

        if (person.getDepartment() != null && person.getDepartment().getHeadOfDepartment() != null &&
                person.getDepartment().getHeadOfDepartment().getId().equals(id)) {
            person.getDepartment().setHeadOfDepartment(null);
        }

        personRepository.deleteById(id);
    }


}