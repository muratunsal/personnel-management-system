package com.example.personnelservice.service;

import com.example.personnelservice.dto.CreatePersonRequest;
import com.example.personnelservice.dto.UpdatePersonRequest;
import com.example.personnelservice.model.Department;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.model.Title;
import com.example.personnelservice.model.Meeting;
import com.example.personnelservice.model.Task;
import com.example.personnelservice.repository.DepartmentRepository;
import com.example.personnelservice.repository.PersonRepository;
import com.example.personnelservice.repository.TitleRepository;
import com.example.personnelservice.repository.MeetingRepository;
import com.example.personnelservice.repository.TaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.personnelservice.config.RabbitMQConfig;
import com.example.personnelservice.event.PersonUpdateEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional
public class PersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);
    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;
    private final TitleRepository titleRepository;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;
    private final com.example.personnelservice.service.AuthService authService;
    private final RabbitTemplate rabbitTemplate;

    public PersonService(PersonRepository personRepository, 
                        DepartmentRepository departmentRepository, 
                        TitleRepository titleRepository,
                        MeetingRepository meetingRepository,
                        TaskRepository taskRepository,
                        com.example.personnelservice.service.AuthService authService,
                        RabbitTemplate rabbitTemplate) {
        this.personRepository = personRepository;
        this.departmentRepository = departmentRepository;
        this.titleRepository = titleRepository;
        this.meetingRepository = meetingRepository;
        this.taskRepository = taskRepository;
        this.authService = authService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Person createPerson(CreatePersonRequest request) {
        log.info("Creating person {}", request.getEmail());
        Person person = new Person();
        if (request.getFirstName() != null) person.setFirstName(request.getFirstName());
        if (request.getLastName() != null) person.setLastName(request.getLastName());
        person.setEmail(request.getEmail());
        person.setPhoneNumber(request.getPhoneNumber());
        if (request.getContractStartDate() != null && !request.getContractStartDate().trim().isEmpty()) {
            try {
                person.setContractStartDate(LocalDate.parse(request.getContractStartDate()));
            } catch (Exception e) {
                throw new RuntimeException("Invalid contract start date format: " + request.getContractStartDate());
            }
        }
        
        if (request.getBirthDate() != null && !request.getBirthDate().trim().isEmpty()) {
            try {
                person.setBirthDate(LocalDate.parse(request.getBirthDate()));
            } catch (Exception e) {
                throw new RuntimeException("Invalid birth date format: " + request.getBirthDate());
            }
        }
        
        person.setGender(request.getGender());
        person.setAddress(request.getAddress());
        person.setProfilePictureUrl(request.getProfilePictureUrl());
        
        if (request.getSalary() != null && !request.getSalary().trim().isEmpty()) {
            try {
                person.setSalary(Integer.parseInt(request.getSalary()));
            } catch (Exception e) {
                throw new RuntimeException("Invalid salary format: " + request.getSalary());
            }
        }
        
        person.setNationalId(request.getNationalId());
        person.setBankAccount(request.getBankAccount());
        person.setInsuranceNumber(request.getInsuranceNumber());
        person.setContractType(request.getContractType());
        
        if (request.getContractEndDate() != null && !request.getContractEndDate().trim().isEmpty()) {
            try {
                person.setContractEndDate(LocalDate.parse(request.getContractEndDate()));
            } catch (Exception e) {
                throw new RuntimeException("Invalid contract end date format: " + request.getContractEndDate());
            }
        }

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
        log.info("Person persisted {}", saved.getId());

        if (targetDepartment != null && targetTitle != null) {
            java.util.List<String> roles = new java.util.ArrayList<>();
            boolean isHead = ("Head of " + targetDepartment.getName()).equalsIgnoreCase(targetTitle.getName());
            boolean isHr = "HR".equalsIgnoreCase(targetDepartment.getName());
            if (isHr) roles.add("HR");
            if (isHead) roles.add("HEAD");
            if (roles.isEmpty()) roles.add("EMPLOYEE");
            log.info("Provisioning user {} with roles {}", saved.getEmail(), roles);
            authService.provision(saved.getEmail(), roles);
        }

        if (targetDepartment != null && targetTitle != null) {
            String expectedHeadTitle = "Head of " + targetDepartment.getName();
            if (expectedHeadTitle.equalsIgnoreCase(targetTitle.getName())) {
                targetDepartment.setHeadOfDepartment(saved);
                departmentRepository.save(targetDepartment);
                log.info("Department head set {} -> {}", targetDepartment.getId(), saved.getId());
            }
        }

        return saved;
    }

    public Page<Person> findPeopleWithFilters(
            String searchQuery, String firstName, String lastName, String email, String phoneNumber,
            Long departmentId, Long titleId, String gender, String address,
            LocalDate contractStartDateFrom, LocalDate contractStartDateTo,
            LocalDate birthDateFrom, LocalDate birthDateTo, Pageable pageable) {
        
        log.info("Searching people with filters");
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
        log.info("Updating person {}", id);
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with ID: " + id));

        
        Map<String, Object> changes = detectChanges(person, request);

        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        Long oldDepartmentId = person.getDepartment() != null ? person.getDepartment().getId() : null;
        Long oldTitleId = person.getTitle() != null ? person.getTitle().getId() : null;
        String oldEmail = person.getEmail();
        if (request.getEmail() != null) person.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) person.setPhoneNumber(request.getPhoneNumber());
        
        if (request.getContractStartDate() != null && !request.getContractStartDate().trim().isEmpty()) {
            try {
                person.setContractStartDate(LocalDate.parse(request.getContractStartDate()));
            } catch (Exception e) {
                throw new RuntimeException("Invalid contract start date format: " + request.getContractStartDate());
            }
        } else {
            person.setContractStartDate(null);
        }
        
        if (request.getBirthDate() != null && !request.getBirthDate().trim().isEmpty()) {
            try {
                person.setBirthDate(LocalDate.parse(request.getBirthDate()));
            } catch (Exception e) {
                throw new RuntimeException("Invalid birth date format: " + request.getBirthDate());
            }
        } else {
            person.setBirthDate(null);
        }
        
        if (request.getGender() != null) person.setGender(request.getGender());
        if (request.getAddress() != null) person.setAddress(request.getAddress());
        if (request.getProfilePictureUrl() != null) person.setProfilePictureUrl(request.getProfilePictureUrl());
        
        if (request.getSalary() != null && !request.getSalary().trim().isEmpty()) {
            try {
                person.setSalary(Integer.parseInt(request.getSalary()));
            } catch (Exception e) {
                throw new RuntimeException("Invalid salary format: " + request.getSalary());
            }
        } else {
            person.setSalary(null);
        }
        
        if (request.getNationalId() != null) person.setNationalId(request.getNationalId());
        if (request.getBankAccount() != null) person.setBankAccount(request.getBankAccount());
        if (request.getInsuranceNumber() != null) person.setInsuranceNumber(request.getInsuranceNumber());
        if (request.getContractType() != null) person.setContractType(request.getContractType());
        
        if (request.getContractEndDate() != null && !request.getContractEndDate().trim().isEmpty()) {
            try {
                person.setContractEndDate(LocalDate.parse(request.getContractEndDate()));
            } catch (Exception e) {
                throw new RuntimeException("Invalid contract end date format: " + request.getContractEndDate());
            }
        } else {
            person.setContractEndDate(null);
        }

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
        log.info("Person saved {}", savedPerson.getId());

        if (!changes.isEmpty()) {
            log.info("Sending person update events {} changes", changes.size());
            sendPersonUpdateEvents(changes, savedPerson);
        } else {
            log.debug("No changes detected for person {}", savedPerson.getId());
        }

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
                log.info("User not found in auth, provisioning {}", savedPerson.getEmail());
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
            log.info("Provisioning new user {} with roles {}", savedPerson.getEmail(), roles);
            authService.provision(savedPerson.getEmail(), roles);
        } else if (!oldEmail.equalsIgnoreCase(savedPerson.getEmail())) {
            log.info("Updating auth email from {} to {}", oldEmail, savedPerson.getEmail());
            authService.updateUser(oldEmail, savedPerson.getEmail(), null);
        }
        
        if (savedPerson.getDepartment() != null && savedPerson.getTitle() != null) {
            java.util.List<String> roles = new java.util.ArrayList<>();
            boolean isHead = savedPerson.getTitle().getName().toLowerCase().startsWith("head of");
            boolean isHr = "HR".equalsIgnoreCase(savedPerson.getDepartment().getName());
            if (isHr) roles.add("HR");
            if (isHead) roles.add("HEAD");
            if (roles.isEmpty()) roles.add("EMPLOYEE");
            log.info("Ensuring user provisioned {} with roles {}", savedPerson.getEmail(), roles);
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
                    log.info("Removed head of department {} due to title change", targetDepartment.getId());
                }
            }
        }

        return savedPerson;
    }

    private Map<String, Object> detectChanges(Person person, UpdatePersonRequest request) {
        Map<String, Object> changes = new HashMap<>();
        
        if (request.getFirstName() != null && !request.getFirstName().equals(person.getFirstName())) {
            changes.put("firstName", Map.of("old", person.getFirstName(), "new", request.getFirstName()));
        }
        if (request.getLastName() != null && !request.getLastName().equals(person.getLastName())) {
            changes.put("lastName", Map.of("old", person.getLastName(), "new", request.getLastName()));
        }
        if (request.getEmail() != null && !request.getEmail().equals(person.getEmail())) {
            changes.put("email", Map.of("old", person.getEmail(), "new", request.getEmail()));
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(person.getPhoneNumber())) {
            changes.put("phoneNumber", Map.of("old", person.getPhoneNumber(), "new", request.getPhoneNumber()));
        }
        if (request.getSalary() != null) {
            if (request.getSalary().trim().isEmpty()) {
                if (person.getSalary() != null) {
                    Map<String, Object> salaryChange = new HashMap<>();
                    salaryChange.put("old", person.getSalary());
                    salaryChange.put("new", null);
                    changes.put("salary", salaryChange);
                }
            } else {
                try {
                    Integer newSalary = Integer.parseInt(request.getSalary());
                    if (!newSalary.equals(person.getSalary())) {
                        Map<String, Object> salaryChange = new HashMap<>();
                        salaryChange.put("old", person.getSalary());
                        salaryChange.put("new", newSalary);
                        changes.put("salary", salaryChange);
                    }
                } catch (Exception e) {
                    
                }
            }
        }
        if (request.getAddress() != null && !request.getAddress().equals(person.getAddress())) {
            changes.put("address", Map.of("old", person.getAddress(), "new", request.getAddress()));
        }
        if (request.getProfilePictureUrl() != null && !request.getProfilePictureUrl().equals(person.getProfilePictureUrl())) {
            changes.put("profilePictureUrl", Map.of("old", person.getProfilePictureUrl(), "new", request.getProfilePictureUrl()));
        }
        if (request.getGender() != null && !request.getGender().equals(person.getGender())) {
            changes.put("gender", Map.of("old", person.getGender(), "new", request.getGender()));
        }
        if (request.getBirthDate() != null) {
            if (request.getBirthDate().trim().isEmpty()) {
                if (person.getBirthDate() != null) {
                    Map<String, Object> birthDateChange = new HashMap<>();
                    birthDateChange.put("old", person.getBirthDate());
                    birthDateChange.put("new", null);
                    changes.put("birthDate", birthDateChange);
                }
            } else {
                try {
                    LocalDate newBirthDate = LocalDate.parse(request.getBirthDate());
                    if (!newBirthDate.equals(person.getBirthDate())) {
                        Map<String, Object> birthDateChange = new HashMap<>();
                        birthDateChange.put("old", person.getBirthDate());
                        birthDateChange.put("new", newBirthDate);
                        changes.put("birthDate", birthDateChange);
                    }
                } catch (Exception e) {
                    
                }
            }
        }
        if (request.getContractStartDate() != null) {
            if (request.getContractStartDate().trim().isEmpty()) {
                if (person.getContractStartDate() != null) {
                    Map<String, Object> contractStartDateChange = new HashMap<>();
                    contractStartDateChange.put("old", person.getContractStartDate());
                    contractStartDateChange.put("new", null);
                    changes.put("contractStartDate", contractStartDateChange);
                }
            } else {
                try {
                    LocalDate newContractStartDate = LocalDate.parse(request.getContractStartDate());
                    if (!newContractStartDate.equals(person.getContractStartDate())) {
                        Map<String, Object> contractStartDateChange = new HashMap<>();
                        contractStartDateChange.put("old", person.getContractStartDate());
                        contractStartDateChange.put("new", newContractStartDate);
                        changes.put("contractStartDate", contractStartDateChange);
                    }
                } catch (Exception e) {
                    
                }
            }
        }
        if (request.getContractEndDate() != null) {
            if (request.getContractEndDate().trim().isEmpty()) {
                if (person.getContractEndDate() != null) {
                    Map<String, Object> contractEndDateChange = new HashMap<>();
                    contractEndDateChange.put("old", person.getContractEndDate());
                    contractEndDateChange.put("new", null);
                    changes.put("contractEndDate", contractEndDateChange);
                }
            } else {
                try {
                    LocalDate newContractEndDate = LocalDate.parse(request.getContractEndDate());
                    if (!newContractEndDate.equals(person.getContractEndDate())) {
                        Map<String, Object> contractEndDateChange = new HashMap<>();
                        contractEndDateChange.put("old", person.getContractEndDate());
                        contractEndDateChange.put("new", newContractEndDate);
                        changes.put("contractEndDate", contractEndDateChange);
                    }
                } catch (Exception e) {
                    
                }
            }
        }
        if (request.getContractType() != null && !request.getContractType().equals(person.getContractType())) {
            changes.put("contractType", Map.of("old", person.getContractType(), "new", request.getContractType()));
        }
        if (request.getNationalId() != null && !request.getNationalId().equals(person.getNationalId())) {
            changes.put("nationalId", Map.of("old", person.getNationalId(), "new", request.getNationalId()));
        }
        if (request.getBankAccount() != null && !request.getBankAccount().equals(person.getBankAccount())) {
            changes.put("bankAccount", Map.of("old", person.getBankAccount(), "new", request.getBankAccount()));
        }
        if (request.getInsuranceNumber() != null && !request.getInsuranceNumber().equals(person.getInsuranceNumber())) {
            changes.put("insuranceNumber", Map.of("old", person.getInsuranceNumber(), "new", request.getInsuranceNumber()));
        }
        if ((request.getDepartmentId() != null && (person.getDepartment() == null || !request.getDepartmentId().equals(person.getDepartment().getId())))
            || (request.getDepartmentId() == null && person.getDepartment() != null)) {
            String oldDept = person.getDepartment() != null ? person.getDepartment().getName() : null;
            String newDept = null;
            if (request.getDepartmentId() != null && request.getDepartmentId() > 0) {
                Department d = departmentRepository.findById(request.getDepartmentId()).orElse(null);
                newDept = d != null ? d.getName() : null;
            }
            changes.put("departmentId", Map.of("old", oldDept, "new", newDept));
        }
        if ((request.getTitleId() != null && (person.getTitle() == null || !request.getTitleId().equals(person.getTitle().getId())))
            || (request.getTitleId() == null && person.getTitle() != null)) {
            String oldTitle = person.getTitle() != null ? person.getTitle().getName() : null;
            String newTitle = null;
            if (request.getTitleId() != null && request.getTitleId() > 0) {
                Title t = titleRepository.findById(request.getTitleId()).orElse(null);
                newTitle = t != null ? t.getName() : null;
            }
            changes.put("titleId", Map.of("old", oldTitle, "new", newTitle));
        }
        
        return changes;
    }

    private void sendPersonUpdateEvents(Map<String, Object> changes, Person person) {
        log.debug("Preparing person update event for {}", person.getId());
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String actorEmail = null;
        String actorName = null;
        Long actorId = null;
        boolean isAdmin = false;
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal != null) {
                actorEmail = principal.toString();
            }
            if (auth.getAuthorities() != null) {
                for (org.springframework.security.core.GrantedAuthority ga : auth.getAuthorities()) {
                    if (ga != null && ga.getAuthority() != null && ga.getAuthority().equals("ROLE_ADMIN")) {
                        isAdmin = true;
                        break;
                    }
                }
            }
        }
        if (isAdmin) {
            actorName = "Admin";
        } else if (actorEmail != null && !actorEmail.isBlank()) {
            Person actor = personRepository.findByEmail(actorEmail);
            if (actor != null) {
                actorName = (actor.getFirstName() != null ? actor.getFirstName() : "") + " " + (actor.getLastName() != null ? actor.getLastName() : "");
                actorId = actor.getId();
            }
        }
        if (actorName == null || actorName.isBlank()) {
            actorName = person.getFirstName() + " " + person.getLastName();
        }
        if (actorEmail == null || actorEmail.isBlank()) {
            actorEmail = person.getEmail();
        }
        if (actorId == null) {
            actorId = person.getId();
        }

        java.util.List<PersonUpdateEvent.ChangeDetail> changeList = new java.util.ArrayList<>();
        java.util.List<String> orderedKeys = java.util.Arrays.asList(
            "firstName",
            "lastName",
            "email",
            "phoneNumber",
            "nationalId",
            "departmentId",
            "titleId",
            "contractType",
            "salary",
            "contractStartDate",
            "contractEndDate",
            "birthDate",
            "gender",
            "address",
            "bankAccount",
            "insuranceNumber",
            "profilePictureUrl"
        );
        java.util.Set<String> added = new java.util.HashSet<>();
        for (String key : orderedKeys) {
            if (changes.containsKey(key)) {
                Map<String, Object> fieldChange = (Map<String, Object>) changes.get(key);
                String oldV = fieldChange.get("old") != null ? String.valueOf(fieldChange.get("old")) : null;
                String newV = fieldChange.get("new") != null ? String.valueOf(fieldChange.get("new")) : null;
                changeList.add(new PersonUpdateEvent.ChangeDetail(key, oldV, newV));
                added.add(key);
            }
        }
        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            if (added.contains(entry.getKey())) continue;
            Map<String, Object> fieldChange = (Map<String, Object>) entry.getValue();
            String k = entry.getKey();
            String oldV = fieldChange.get("old") != null ? String.valueOf(fieldChange.get("old")) : null;
            String newV = fieldChange.get("new") != null ? String.valueOf(fieldChange.get("new")) : null;
            changeList.add(new PersonUpdateEvent.ChangeDetail(k, oldV, newV));
        }

        PersonUpdateEvent event = new PersonUpdateEvent();
        event.setPersonId(person.getId());
        event.setUpdatedBy(actorId);
        event.setUpdatedAt(LocalDateTime.now());
        event.setPersonEmail(person.getEmail());
        event.setPersonName(person.getFirstName() + " " + person.getLastName());
        event.setUpdatedByEmail(actorEmail);
        event.setUpdatedByName(actorName);
        event.setChanges(changeList);

        rabbitTemplate.convertAndSend(RabbitMQConfig.PERSON_UPDATE_EXCHANGE, 
                                      RabbitMQConfig.PERSON_UPDATE_ROUTING_KEY, event);
        log.info("Person update event sent {}", person.getId());
        
    }

    public void deletePerson(Long id) {
        log.info("Deleting person {}", id);
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with ID: " + id));

        java.util.List<com.example.personnelservice.model.Department> departments = departmentRepository.findByHeadOfDepartmentId(id);
        for (com.example.personnelservice.model.Department dept : departments) {
            dept.setHeadOfDepartment(null);
            departmentRepository.save(dept);
        }

        if (person.getDepartment() != null && person.getDepartment().getHeadOfDepartment() != null &&
                person.getDepartment().getHeadOfDepartment().getId().equals(id)) {
            person.getDepartment().setHeadOfDepartment(null);
            departmentRepository.save(person.getDepartment());
        }

        java.util.List<Meeting> meetingsAsOrganizer = meetingRepository.findByOrganizerOrderByDayAscStartTimeAsc(person);
        for (Meeting meeting : meetingsAsOrganizer) {
            meeting.setOrganizer(null);
            meetingRepository.save(meeting);
        }

        java.util.List<Meeting> meetingsAsParticipant = meetingRepository.findByParticipantsContainsOrderByDayAscStartTimeAsc(person);
        for (Meeting meeting : meetingsAsParticipant) {
            meeting.getParticipants().remove(person);
            meetingRepository.save(meeting);
        }

        java.util.List<Task> tasksAsAssignee = taskRepository.findByAssigneeOrderByCreatedAtDesc(person);
        for (Task task : tasksAsAssignee) {
            task.setAssignee(null);
            taskRepository.save(task);
        }

        java.util.List<Task> tasksAsCreator = taskRepository.findByCreatedByOrderByCreatedAtDesc(person);
        for (Task task : tasksAsCreator) {
            task.setCreatedBy(null);
            taskRepository.save(task);
        }

        personRepository.deleteById(id);
        log.info("Person deleted {}", id);
    }


}