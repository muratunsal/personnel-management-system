package com.example.personnelservice.config;

import com.example.personnelservice.model.Department;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.model.Title;
import com.example.personnelservice.model.Task;
import com.example.personnelservice.model.Meeting;
import com.example.personnelservice.repository.DepartmentRepository;
import com.example.personnelservice.repository.PersonRepository;
import com.example.personnelservice.repository.TitleRepository;
import com.example.personnelservice.repository.TaskRepository;
import com.example.personnelservice.repository.MeetingRepository;
import com.example.personnelservice.service.AuthService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashSet;

@Configuration
public class DataInitializer {



    @Bean
    CommandLineRunner seedData(PersonRepository personRepository, 
                              DepartmentRepository departmentRepository, 
                              TitleRepository titleRepository, 
                              TaskRepository taskRepository,
                              MeetingRepository meetingRepository,
                              AuthService authService) {
        return args -> {
            if (personRepository.count() > 0) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            Random random = new Random();

            Map<String, String> departmentColors = new HashMap<>();
            departmentColors.put("Engineering", "#3b82f6");
            departmentColors.put("HR", "#14b8a6");
            departmentColors.put("Product", "#8b5cf6");
            departmentColors.put("Design", "#f472b6");
            departmentColors.put("Sales", "#f59e0b");

            Map<String, Department> nameToDept = new HashMap<>();
            for (Map.Entry<String, String> e : departmentColors.entrySet()) {
                Department d = new Department();
                d.setName(e.getKey());
                d.setColor(e.getValue());
                nameToDept.put(e.getKey(), departmentRepository.save(d));
            }

            Map<String, List<String>> departmentTitles = new HashMap<>();
            departmentTitles.put("Engineering", List.of("Software Engineer", "Senior Developer", "DevOps Engineer", "QA Engineer", "Head of Engineering"));
            departmentTitles.put("HR", List.of("HR Specialist", "Recruiter", "Head of HR"));
            departmentTitles.put("Product", List.of("Product Manager", "Product Owner", "Head of Product"));
            departmentTitles.put("Design", List.of("UX Designer", "UI Designer", "Head of Design"));
            departmentTitles.put("Sales", List.of("Sales Executive", "Account Manager", "Head of Sales"));

            Map<String, Title> nameToTitle = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : departmentTitles.entrySet()) {
                Department dept = nameToDept.get(entry.getKey());
                for (String titleName : entry.getValue()) {
                    Title title = new Title();
                    title.setName(titleName);
                    title.setDepartment(dept);
                    nameToTitle.put(titleName, titleRepository.save(title));
                }
            }

            List<Person> people = new ArrayList<>();
            
            people.add(create("Michael", "Johnson", "michael.johnson@example.com", "+1 555 123 4567", "Engineering", "Head of Engineering", 
                LocalDate.of(2018, 3, 15), LocalDate.of(1985, 7, 22), "Male", "San Francisco, CA", 
                "https://randomuser.me/api/portraits/men/1.jpg", nameToDept, nameToTitle, 120000));
            
            people.add(create("Sarah", "Williams", "sarah.williams@example.com", "+1 555 234 5678", "HR", "Head of HR", 
                LocalDate.of(2019, 1, 10), LocalDate.of(1988, 4, 15), "Female", "New York, NY", 
                "https://randomuser.me/api/portraits/women/2.jpg", nameToDept, nameToTitle, 95000));
            
            people.add(create("David", "Brown", "david.brown@example.com", "+1 555 345 6789", "Product", "Head of Product", 
                LocalDate.of(2017, 6, 20), LocalDate.of(1983, 11, 8), "Male", "Seattle, WA", 
                "https://randomuser.me/api/portraits/men/3.jpg", nameToDept, nameToTitle, 110000));
            
            people.add(create("Emily", "Davis", "emily.davis@example.com", "+1 555 456 7890", "Design", "Head of Design", 
                LocalDate.of(2018, 9, 5), LocalDate.of(1986, 2, 28), "Female", "Austin, TX", 
                "https://randomuser.me/api/portraits/women/4.jpg", nameToDept, nameToTitle, 100000));
            
            people.add(create("James", "Miller", "james.miller@example.com", "+1 555 567 8901", "Sales", "Head of Sales", 
                LocalDate.of(2019, 4, 12), LocalDate.of(1984, 9, 14), "Male", "Boston, MA", 
                "https://randomuser.me/api/portraits/men/5.jpg", nameToDept, nameToTitle, 105000));

            people.add(create("Jennifer", "Wilson", "jennifer.wilson@example.com", "+1 555 678 9012", "Engineering", "Senior Developer", 
                LocalDate.of(2020, 2, 1), LocalDate.of(1990, 12, 3), "Female", "San Francisco, CA", 
                "https://randomuser.me/api/portraits/women/6.jpg", nameToDept, nameToTitle, 85000));
            
            people.add(create("Robert", "Taylor", "robert.taylor@example.com", "+1 555 789 0123", "Engineering", "Software Engineer", 
                LocalDate.of(2021, 7, 15), LocalDate.of(1992, 5, 18), "Male", "San Francisco, CA", 
                "https://randomuser.me/api/portraits/men/7.jpg", nameToDept, nameToTitle, 65000));
            
            people.add(create("Christopher", "Anderson", "christopher.anderson@example.com", "+1 555 890 1234", "Engineering", "DevOps Engineer", 
                LocalDate.of(2020, 11, 8), LocalDate.of(1989, 8, 25), "Male", "San Francisco, CA", 
                "https://randomuser.me/api/portraits/men/8.jpg", nameToDept, nameToTitle, 75000));
            
            people.add(create("Amanda", "Thomas", "amanda.thomas@example.com", "+1 555 901 2345", "Engineering", "QA Engineer", 
                LocalDate.of(2021, 3, 22), LocalDate.of(1991, 1, 10), "Female", "San Francisco, CA", 
                "https://randomuser.me/api/portraits/women/9.jpg", nameToDept, nameToTitle, 60000));
            
            people.add(create("Daniel", "Jackson", "daniel.jackson@example.com", "+1 555 012 3456", "Product", "Product Manager", 
                LocalDate.of(2020, 5, 18), LocalDate.of(1987, 6, 12), "Male", "Seattle, WA", 
                "https://randomuser.me/api/portraits/men/10.jpg", nameToDept, nameToTitle, 80000));
            
            people.add(create("Jessica", "White", "jessica.white@example.com", "+1 555 123 4567", "Product", "Product Owner", 
                LocalDate.of(2021, 9, 30), LocalDate.of(1993, 3, 7), "Female", "Seattle, WA", 
                "https://randomuser.me/api/portraits/women/11.jpg", nameToDept, nameToTitle, 70000));
            
            people.add(create("Matthew", "Harris", "matthew.harris@example.com", "+1 555 234 5678", "Design", "UX Designer", 
                LocalDate.of(2020, 8, 14), LocalDate.of(1990, 10, 20), "Male", "Austin, TX", 
                "https://randomuser.me/api/portraits/men/12.jpg", nameToDept, nameToTitle, 65000));
            
            people.add(create("Ashley", "Martin", "ashley.martin@example.com", "+1 555 345 6789", "Design", "UI Designer", 
                LocalDate.of(2021, 1, 25), LocalDate.of(1992, 7, 15), "Female", "Austin, TX", 
                "https://randomuser.me/api/portraits/women/13.jpg", nameToDept, nameToTitle, 60000));
            
            people.add(create("Andrew", "Thompson", "andrew.thompson@example.com", "+1 555 456 7890", "Sales", "Sales Executive", 
                LocalDate.of(2020, 12, 3), LocalDate.of(1988, 4, 9), "Male", "Boston, MA", 
                "https://randomuser.me/api/portraits/men/14.jpg", nameToDept, nameToTitle, 55000));
            
            people.add(create("Nicole", "Garcia", "nicole.garcia@example.com", "+1 555 567 8901", "Sales", "Account Manager", 
                LocalDate.of(2021, 6, 17), LocalDate.of(1991, 11, 28), "Female", "Boston, MA", 
                "https://randomuser.me/api/portraits/women/15.jpg", nameToDept, nameToTitle, 60000));
            
            people.add(create("Kevin", "Martinez", "kevin.martinez@example.com", "+1 555 678 9012", "HR", "HR Specialist", 
                LocalDate.of(2020, 4, 8), LocalDate.of(1989, 12, 5), "Male", "New York, NY", 
                "https://randomuser.me/api/portraits/men/16.jpg", nameToDept, nameToTitle, 55000));
            
            people.add(create("Lisa", "Robinson", "lisa.robinson@example.com", "+1 555 789 0123", "HR", "Recruiter", 
                LocalDate.of(2021, 2, 14), LocalDate.of(1993, 9, 18), "Female", "New York, NY", 
                "https://randomuser.me/api/portraits/women/17.jpg", nameToDept, nameToTitle, 50000));

            for (Person p : people) {
                List<String> roles = new ArrayList<>();
                boolean isHead = p.getTitle() != null && p.getTitle().getName().toLowerCase().contains("head");
                boolean isHr = p.getDepartment() != null && "HR".equalsIgnoreCase(p.getDepartment().getName());
                if (isHr) roles.add("HR");
                if (isHead) roles.add("HEAD");
                if (roles.isEmpty()) roles.add("EMPLOYEE");
                authService.provisionWithPassword(p.getEmail(), roles, "123456");
            }
            
            List<Person> savedPeople = personRepository.saveAll(people);

            setHeadOfDepartment("Engineering", "Michael", "Johnson", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("HR", "Sarah", "Williams", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Product", "David", "Brown", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Design", "Emily", "Davis", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Sales", "James", "Miller", nameToDept, personRepository, departmentRepository);

            createTasks(taskRepository, personRepository, now, random);
            createMeetings(meetingRepository, personRepository, now, random);
        };
    }

    private Person create(String firstName, String lastName, String email, String phone, String department, String title,
                           LocalDate startDate, LocalDate birthDate, String gender, String address, String ppUrl,
                           Map<String, Department> nameToDept, Map<String, Title> nameToTitle, int baseSalary) {
        Person p = new Person();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setEmail(email);
        p.setPhoneNumber(phone);
        p.setDepartment(nameToDept.get(department));
        p.setTitle(nameToTitle.get(title));
        p.setContractStartDate(startDate);
        p.setBirthDate(birthDate);
        p.setGender(gender);
        p.setAddress(address);
        p.setProfilePictureUrl(ppUrl);
        
        p.setSalary(baseSalary + (int)(Math.random() * 20000));
        p.setNationalId(String.format("%03d-%02d-%04d", (int)(Math.random() * 1000), (int)(Math.random() * 100), (int)(Math.random() * 10000)));
        p.setBankAccount(String.format("%012d", (int)(Math.random() * 1000000000000L)));
        p.setInsuranceNumber(String.format("%03d-%02d-%04d", (int)(Math.random() * 1000), (int)(Math.random() * 100), (int)(Math.random() * 10000)));
        
        if (title.toLowerCase().contains("head")) {
            p.setContractType("Full-time");
        } else if (Math.random() < 0.1) {
            p.setContractType("Part-time");
        } else {
            p.setContractType("Full-time");
        }
        
        p.setContractEndDate(LocalDate.now().plusYears(3));
        
        return p;
    }

    private void setHeadOfDepartment(String departmentName, String firstName, String lastName, 
                                   Map<String, Department> nameToDept, PersonRepository personRepository, 
                                   DepartmentRepository departmentRepository) {
        Department dept = nameToDept.get(departmentName);
        Person head = personRepository.findByFirstNameAndLastName(firstName, lastName);
        if (dept != null && head != null) {
            dept.setHeadOfDepartment(head);
            departmentRepository.save(dept);
        }
    }

    private void createTasks(TaskRepository taskRepository, PersonRepository personRepository, LocalDateTime now, Random random) {
        List<Task> tasks = new ArrayList<>();
        
        Person hrHead = personRepository.findByFirstNameAndLastName("Sarah", "Williams");
        Person engHead = personRepository.findByFirstNameAndLastName("Michael", "Johnson");
        Person productHead = personRepository.findByFirstNameAndLastName("David", "Brown");
        Person designHead = personRepository.findByFirstNameAndLastName("Emily", "Davis");
        Person salesHead = personRepository.findByFirstNameAndLastName("James", "Miller");

        List<Person> allPeople = personRepository.findAll();
        
        tasks.add(createTask("Employee Performance Review Q4", "Complete annual performance reviews for all team members", 
            "HIGH", "IN_PROGRESS", hrHead, 
            personRepository.findByFirstNameAndLastName("Kevin", "Martinez")));
        
        tasks.add(createTask("Hiring New Developers", "Interview and hire 2 new software engineers", 
            "HIGH", "ASSIGNED", hrHead, 
            personRepository.findByFirstNameAndLastName("Lisa", "Robinson")));
        
        tasks.add(createTask("Employee Onboarding", "Improve employee onboarding process", 
            "MEDIUM", "CLOSED", hrHead, 
            personRepository.findByFirstNameAndLastName("Kevin", "Martinez")));
        
        tasks.add(createTask("Benefits Package Review", "Review and update employee benefits package", 
            "MEDIUM", "IN_PROGRESS", hrHead, 
            personRepository.findByFirstNameAndLastName("Lisa", "Robinson")));
        
        tasks.add(createTask("Workplace Safety Audit", "Conduct quarterly workplace safety inspection", 
            "HIGH", "ASSIGNED", hrHead, 
            personRepository.findByFirstNameAndLastName("Kevin", "Martinez")));
        
        tasks.add(createTask("Employee Training Program", "Develop new employee training curriculum", 
            "MEDIUM", "COMPLETED", hrHead, 
            personRepository.findByFirstNameAndLastName("Lisa", "Robinson")));
        
        tasks.add(createTask("Diversity & Inclusion Initiative", "Plan and implement D&I programs", 
            "LOW", "IN_PROGRESS", hrHead, 
            personRepository.findByFirstNameAndLastName("Kevin", "Martinez")));
        
        tasks.add(createTask("Exit Interview Process", "Standardize exit interview procedures", 
            "LOW", "CLOSED", hrHead, 
            personRepository.findByFirstNameAndLastName("Lisa", "Robinson")));

        tasks.add(createTask("Database Migration", "Migrate production database to new version", 
            "CRITICAL", "IN_PROGRESS", engHead, 
            personRepository.findByFirstNameAndLastName("Christopher", "Anderson")));
        
        tasks.add(createTask("Code Review Process", "Implement new code review guidelines", 
            "MEDIUM", "CLOSED", engHead, 
            personRepository.findByFirstNameAndLastName("Jennifer", "Wilson")));
        
        tasks.add(createTask("API Documentation Update", "Update API documentation for new endpoints", 
            "LOW", "CLOSED", engHead, 
            personRepository.findByFirstNameAndLastName("Robert", "Taylor")));
        
        tasks.add(createTask("Security Audit", "Conduct quarterly security audit", 
            "CRITICAL", "ASSIGNED", engHead, 
            personRepository.findByFirstNameAndLastName("Amanda", "Thomas")));
        
        tasks.add(createTask("Performance Testing", "Run performance tests on new features", 
            "MEDIUM", "IN_PROGRESS", engHead, 
            personRepository.findByFirstNameAndLastName("Jennifer", "Wilson")));
        
        tasks.add(createTask("Infrastructure Scaling", "Scale cloud infrastructure for increased load", 
            "HIGH", "ASSIGNED", engHead, 
            personRepository.findByFirstNameAndLastName("Christopher", "Anderson")));
        
        tasks.add(createTask("Code Quality Metrics", "Implement automated code quality checks", 
            "MEDIUM", "COMPLETED", engHead, 
            personRepository.findByFirstNameAndLastName("Robert", "Taylor")));
        
        tasks.add(createTask("Deployment Automation", "Automate CI/CD pipeline deployment", 
            "HIGH", "IN_PROGRESS", engHead, 
            personRepository.findByFirstNameAndLastName("Christopher", "Anderson")));
        
        tasks.add(createTask("Technical Debt Reduction", "Refactor legacy code components", 
            "LOW", "ASSIGNED", engHead, 
            personRepository.findByFirstNameAndLastName("Jennifer", "Wilson")));
        
        tasks.add(createTask("Monitoring Setup", "Implement comprehensive system monitoring", 
            "MEDIUM", "CLOSED", engHead, 
            personRepository.findByFirstNameAndLastName("Amanda", "Thomas")));
        
        tasks.add(createTask("Backup System Upgrade", "Upgrade data backup and recovery systems", 
            "HIGH", "IN_PROGRESS", engHead, 
            personRepository.findByFirstNameAndLastName("Christopher", "Anderson")));
        
        tasks.add(createTask("API Rate Limiting", "Implement rate limiting for public APIs", 
            "MEDIUM", "ASSIGNED", engHead, 
            personRepository.findByFirstNameAndLastName("Robert", "Taylor")));

        tasks.add(createTask("Product Roadmap Planning", "Plan product features for next quarter", 
            "HIGH", "IN_PROGRESS", productHead, 
            personRepository.findByFirstNameAndLastName("Jessica", "White")));
        
        tasks.add(createTask("Customer Feedback Analysis", "Analyze customer feedback and create report", 
            "MEDIUM", "COMPLETED", productHead, 
            personRepository.findByFirstNameAndLastName("Daniel", "Jackson")));
        
        tasks.add(createTask("Competitor Analysis", "Research competitor products and market trends", 
            "LOW", "ASSIGNED", productHead, 
            personRepository.findByFirstNameAndLastName("Jessica", "White")));
        
        tasks.add(createTask("User Story Creation", "Create user stories for Q1 features", 
            "MEDIUM", "IN_PROGRESS", productHead, 
            personRepository.findByFirstNameAndLastName("Daniel", "Jackson")));
        
        tasks.add(createTask("Product Metrics Dashboard", "Design and implement product analytics dashboard", 
            "HIGH", "ASSIGNED", productHead, 
            personRepository.findByFirstNameAndLastName("Jessica", "White")));
        
        tasks.add(createTask("A/B Testing Framework", "Set up A/B testing infrastructure", 
            "MEDIUM", "CLOSED", productHead, 
            personRepository.findByFirstNameAndLastName("Daniel", "Jackson")));
        
        tasks.add(createTask("Product Requirements Review", "Review and finalize product requirements", 
            "HIGH", "COMPLETED", productHead, 
            personRepository.findByFirstNameAndLastName("Jessica", "White")));
        
        tasks.add(createTask("Customer Interview Schedule", "Schedule and conduct customer interviews", 
            "MEDIUM", "ASSIGNED", productHead, 
            personRepository.findByFirstNameAndLastName("Daniel", "Jackson")));
        
        tasks.add(createTask("Feature Prioritization", "Prioritize feature backlog for next sprint", 
            "HIGH", "IN_PROGRESS", productHead, 
            personRepository.findByFirstNameAndLastName("Jessica", "White")));
        
        tasks.add(createTask("Product Documentation", "Update product documentation and guides", 
            "LOW", "CLOSED", productHead, 
            personRepository.findByFirstNameAndLastName("Daniel", "Jackson")));

        tasks.add(createTask("User Interface Redesign", "Redesign main dashboard interface", 
            "MEDIUM", "IN_PROGRESS", designHead, 
            personRepository.findByFirstNameAndLastName("Matthew", "Harris")));
        
        tasks.add(createTask("Design System Update", "Update design system with new components", 
            "MEDIUM", "ASSIGNED", designHead, 
            personRepository.findByFirstNameAndLastName("Ashley", "Martin")));
        
        tasks.add(createTask("Mobile App Design", "Design mobile app interface mockups", 
            "HIGH", "COMPLETED", designHead, 
            personRepository.findByFirstNameAndLastName("Matthew", "Harris")));
        
        tasks.add(createTask("Brand Guidelines Update", "Update company brand guidelines", 
            "LOW", "CLOSED", designHead, 
            personRepository.findByFirstNameAndLastName("Ashley", "Martin")));
        
        tasks.add(createTask("User Research Study", "Conduct user research and usability testing", 
            "MEDIUM", "ASSIGNED", designHead, 
            personRepository.findByFirstNameAndLastName("Matthew", "Harris")));
        
        tasks.add(createTask("Icon Set Creation", "Create new icon set for application", 
            "LOW", "IN_PROGRESS", designHead, 
            personRepository.findByFirstNameAndLastName("Ashley", "Martin")));
        
        tasks.add(createTask("Prototype Development", "Develop interactive prototypes for new features", 
            "HIGH", "ASSIGNED", designHead, 
            personRepository.findByFirstNameAndLastName("Matthew", "Harris")));
        
        tasks.add(createTask("Design Review Process", "Establish design review and approval process", 
            "MEDIUM", "CLOSED", designHead, 
            personRepository.findByFirstNameAndLastName("Ashley", "Martin")));
        
        tasks.add(createTask("Accessibility Audit", "Conduct accessibility compliance audit", 
            "HIGH", "COMPLETED", designHead, 
            personRepository.findByFirstNameAndLastName("Matthew", "Harris")));
        
        tasks.add(createTask("Design Asset Management", "Organize and catalog design assets", 
            "LOW", "ASSIGNED", designHead, 
            personRepository.findByFirstNameAndLastName("Ashley", "Martin")));

        tasks.add(createTask("Sales Pipeline Review", "Review and optimize sales pipeline for Q1", 
            "HIGH", "COMPLETED", salesHead, 
            personRepository.findByFirstNameAndLastName("Andrew", "Thompson")));
        
        tasks.add(createTask("Sales Training Program", "Develop new sales training program", 
            "HIGH", "IN_PROGRESS", salesHead, 
            personRepository.findByFirstNameAndLastName("Nicole", "Garcia")));
        
        tasks.add(createTask("Lead Generation Campaign", "Launch new lead generation campaign", 
            "MEDIUM", "ASSIGNED", salesHead, 
            personRepository.findByFirstNameAndLastName("Andrew", "Thompson")));
        
        tasks.add(createTask("Customer Success Metrics", "Define and track customer success metrics", 
            "MEDIUM", "CLOSED", salesHead, 
            personRepository.findByFirstNameAndLastName("Nicole", "Garcia")));
        
        tasks.add(createTask("Sales Territory Planning", "Plan and assign sales territories", 
            "HIGH", "ASSIGNED", salesHead, 
            personRepository.findByFirstNameAndLastName("Andrew", "Thompson")));
        
        tasks.add(createTask("CRM System Optimization", "Optimize CRM system for better tracking", 
            "MEDIUM", "COMPLETED", salesHead, 
            personRepository.findByFirstNameAndLastName("Nicole", "Garcia")));
        
        tasks.add(createTask("Sales Presentation Update", "Update sales presentation materials", 
            "LOW", "ASSIGNED", salesHead, 
            personRepository.findByFirstNameAndLastName("Andrew", "Thompson")));
        
        tasks.add(createTask("Customer Feedback Collection", "Collect feedback from existing customers", 
            "MEDIUM", "IN_PROGRESS", salesHead, 
            personRepository.findByFirstNameAndLastName("Nicole", "Garcia")));
        
        tasks.add(createTask("Sales Forecast Analysis", "Analyze and update sales forecasts", 
            "HIGH", "ASSIGNED", salesHead, 
            personRepository.findByFirstNameAndLastName("Andrew", "Thompson")));
        
        tasks.add(createTask("Partnership Development", "Develop strategic partnerships", 
            "LOW", "CLOSED", salesHead, 
            personRepository.findByFirstNameAndLastName("Nicole", "Garcia")));

        taskRepository.saveAll(tasks);
    }

    private Task createTask(String title, String description, String priority, String status, Person assignedBy, 
                           Person assignee) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(Task.Priority.valueOf(priority));
        task.setStatus(Task.Status.valueOf(status));
        task.setDepartment(assignee.getDepartment());
        task.setCreatedBy(assignedBy);
        task.setAssignee(assignee);
        return task;
    }

    private void createMeetings(MeetingRepository meetingRepository, PersonRepository personRepository, LocalDateTime now, Random random) {
        List<Meeting> meetings = new ArrayList<>();
        
        Person hrHead = personRepository.findByFirstNameAndLastName("Sarah", "Williams");
        Person engHead = personRepository.findByFirstNameAndLastName("Michael", "Johnson");
        Person productHead = personRepository.findByFirstNameAndLastName("David", "Brown");
        Person designHead = personRepository.findByFirstNameAndLastName("Emily", "Davis");
        Person salesHead = personRepository.findByFirstNameAndLastName("James", "Miller");

        List<Person> allPeople = personRepository.findAll();
        
        meetings.add(createMeeting("Weekly Engineering Standup", "Daily standup for engineering team", 
            now.minusDays(1).withHour(9).withMinute(0), now.minusDays(1).withHour(9).withMinute(30), 
            engHead, List.of(personRepository.findByFirstNameAndLastName("Jennifer", "Wilson"), 
                           personRepository.findByFirstNameAndLastName("Robert", "Taylor"),
                           personRepository.findByFirstNameAndLastName("Christopher", "Anderson"),
                           personRepository.findByFirstNameAndLastName("Amanda", "Thomas"))));
        
        meetings.add(createMeeting("Technical Architecture Review", "Review system architecture and technical decisions", 
            now.plusDays(4).withHour(11).withMinute(0), now.plusDays(4).withHour(12).withMinute(30), 
            engHead, List.of(personRepository.findByFirstNameAndLastName("Jennifer", "Wilson"),
                           personRepository.findByFirstNameAndLastName("Christopher", "Anderson"),
                           personRepository.findByFirstNameAndLastName("Robert", "Taylor"))));
        
        meetings.add(createMeeting("Code Quality Review", "Review code quality metrics and improvements", 
            now.plusDays(8).withHour(10).withMinute(0), now.plusDays(8).withHour(11).withMinute(0), 
            engHead, List.of(personRepository.findByFirstNameAndLastName("Jennifer", "Wilson"),
                           personRepository.findByFirstNameAndLastName("Amanda", "Thomas"),
                           personRepository.findByFirstNameAndLastName("Robert", "Taylor"))));
        
        meetings.add(createMeeting("Sprint Planning", "Plan tasks and goals for next sprint", 
            now.plusDays(2).withHour(14).withMinute(0), now.plusDays(2).withHour(15).withMinute(30), 
            engHead, List.of(personRepository.findByFirstNameAndLastName("Jennifer", "Wilson"),
                           personRepository.findByFirstNameAndLastName("Robert", "Taylor"),
                           personRepository.findByFirstNameAndLastName("Christopher", "Anderson"),
                           personRepository.findByFirstNameAndLastName("Amanda", "Thomas"))));
        
        meetings.add(createMeeting("Sprint Retrospective", "Review previous sprint and identify improvements", 
            now.minusDays(3).withHour(16).withMinute(0), now.minusDays(3).withHour(17).withMinute(0), 
            engHead, List.of(personRepository.findByFirstNameAndLastName("Jennifer", "Wilson"),
                           personRepository.findByFirstNameAndLastName("Robert", "Taylor"),
                           personRepository.findByFirstNameAndLastName("Christopher", "Anderson"),
                           personRepository.findByFirstNameAndLastName("Amanda", "Thomas"))));
        
        meetings.add(createMeeting("Technical Debt Discussion", "Discuss and prioritize technical debt items", 
            now.plusDays(6).withHour(13).withMinute(0), now.plusDays(6).withHour(14).withMinute(0), 
            engHead, List.of(personRepository.findByFirstNameAndLastName("Jennifer", "Wilson"),
                           personRepository.findByFirstNameAndLastName("Christopher", "Anderson"))));
        
        meetings.add(createMeeting("Security Review", "Review security policies and recent incidents", 
            now.plusDays(5).withHour(10).withMinute(0), now.plusDays(5).withHour(11).withMinute(0), 
            engHead, List.of(personRepository.findByFirstNameAndLastName("Amanda", "Thomas"),
                           personRepository.findByFirstNameAndLastName("Christopher", "Anderson"))));
        
        meetings.add(createMeeting("Infrastructure Planning", "Plan infrastructure scaling and improvements", 
            now.plusDays(7).withHour(15).withMinute(0), now.plusDays(7).withHour(16).withMinute(30), 
            engHead, List.of(personRepository.findByFirstNameAndLastName("Christopher", "Anderson"),
                           personRepository.findByFirstNameAndLastName("Robert", "Taylor"))));
        
        meetings.add(createMeeting("API Design Review", "Review API design and documentation standards", 
            now.plusDays(9).withHour(14).withMinute(0), now.plusDays(9).withHour(15).withMinute(0), 
            engHead, List.of(personRepository.findByFirstNameAndLastName("Robert", "Taylor"),
                           personRepository.findByFirstNameAndLastName("Jennifer", "Wilson"))));
        
        meetings.add(createMeeting("Testing Strategy", "Discuss testing approach and automation", 
            now.plusDays(10).withHour(11).withMinute(0), now.plusDays(10).withHour(12).withMinute(0), 
            engHead, List.of(personRepository.findByFirstNameAndLastName("Amanda", "Thomas"),
                           personRepository.findByFirstNameAndLastName("Jennifer", "Wilson"))));

        meetings.add(createMeeting("Product Planning Session", "Q1 product planning and roadmap discussion", 
            now.plusDays(2).withHour(14).withMinute(0), now.plusDays(2).withHour(15).withMinute(30), 
            productHead, List.of(personRepository.findByFirstNameAndLastName("Daniel", "Jackson"),
                               personRepository.findByFirstNameAndLastName("Jessica", "White"),
                               personRepository.findByFirstNameAndLastName("Matthew", "Harris"),
                               personRepository.findByFirstNameAndLastName("Ashley", "Martin"))));
        
        meetings.add(createMeeting("Customer Feedback Session", "Review customer feedback and plan improvements", 
            now.plusDays(6).withHour(14).withMinute(0), now.plusDays(6).withHour(15).withMinute(0), 
            productHead, List.of(personRepository.findByFirstNameAndLastName("Daniel", "Jackson"),
                               personRepository.findByFirstNameAndLastName("Jessica", "White"),
                               personRepository.findByFirstNameAndLastName("Matthew", "Harris"))));
        
        meetings.add(createMeeting("Product Requirements Review", "Review and finalize product requirements", 
            now.plusDays(3).withHour(10).withMinute(0), now.plusDays(3).withHour(11).withMinute(30), 
            productHead, List.of(personRepository.findByFirstNameAndLastName("Daniel", "Jackson"),
                               personRepository.findByFirstNameAndLastName("Jessica", "White"))));
        
        meetings.add(createMeeting("User Story Workshop", "Create and refine user stories for next sprint", 
            now.plusDays(5).withHour(13).withMinute(0), now.plusDays(5).withHour(15).withMinute(0), 
            productHead, List.of(personRepository.findByFirstNameAndLastName("Daniel", "Jackson"),
                               personRepository.findByFirstNameAndLastName("Jessica", "White"),
                               personRepository.findByFirstNameAndLastName("Matthew", "Harris"))));
        
        meetings.add(createMeeting("Competitor Analysis Review", "Review competitor research findings", 
            now.plusDays(8).withHour(14).withMinute(0), now.plusDays(8).withHour(15).withMinute(0), 
            productHead, List.of(personRepository.findByFirstNameAndLastName("Jessica", "White"),
                               personRepository.findByFirstNameAndLastName("Daniel", "Jackson"))));
        
        meetings.add(createMeeting("Product Metrics Review", "Review product performance metrics", 
            now.plusDays(4).withHour(16).withMinute(0), now.plusDays(4).withHour(17).withMinute(0), 
            productHead, List.of(personRepository.findByFirstNameAndLastName("Jessica", "White"),
                               personRepository.findByFirstNameAndLastName("Daniel", "Jackson"))));
        
        meetings.add(createMeeting("Feature Prioritization", "Prioritize feature backlog for next quarter", 
            now.plusDays(7).withHour(10).withMinute(0), now.plusDays(7).withHour(11).withMinute(30), 
            productHead, List.of(personRepository.findByFirstNameAndLastName("Daniel", "Jackson"),
                               personRepository.findByFirstNameAndLastName("Jessica", "White"))));
        
        meetings.add(createMeeting("Customer Interview Planning", "Plan customer interview schedule and questions", 
            now.plusDays(9).withHour(13).withMinute(0), now.plusDays(9).withHour(14).withMinute(0), 
            productHead, List.of(personRepository.findByFirstNameAndLastName("Jessica", "White"),
                               personRepository.findByFirstNameAndLastName("Daniel", "Jackson"))));

        meetings.add(createMeeting("Design Review", "Review new design concepts and prototypes", 
            now.plusDays(1).withHour(15).withMinute(0), now.plusDays(1).withHour(16).withMinute(0), 
            designHead, List.of(personRepository.findByFirstNameAndLastName("Matthew", "Harris"),
                               personRepository.findByFirstNameAndLastName("Ashley", "Martin"))));
        
        meetings.add(createMeeting("Design System Workshop", "Collaborate on design system updates", 
            now.plusDays(3).withHour(14).withMinute(0), now.plusDays(3).withHour(15).withMinute(30), 
            designHead, List.of(personRepository.findByFirstNameAndLastName("Matthew", "Harris"),
                               personRepository.findByFirstNameAndLastName("Ashley", "Martin"))));
        
        meetings.add(createMeeting("User Research Planning", "Plan user research studies and methodologies", 
            now.plusDays(5).withHour(11).withMinute(0), now.plusDays(5).withHour(12).withMinute(0), 
            designHead, List.of(personRepository.findByFirstNameAndLastName("Matthew", "Harris"),
                               personRepository.findByFirstNameAndLastName("Ashley", "Martin"))));
        
        meetings.add(createMeeting("Brand Guidelines Review", "Review and update brand guidelines", 
            now.plusDays(7).withHour(14).withMinute(0), now.plusDays(7).withHour(15).withMinute(0), 
            designHead, List.of(personRepository.findByFirstNameAndLastName("Ashley", "Martin"),
                               personRepository.findByFirstNameAndLastName("Matthew", "Harris"))));
        
        meetings.add(createMeeting("Accessibility Workshop", "Discuss accessibility improvements and compliance", 
            now.plusDays(8).withHour(13).withMinute(0), now.plusDays(8).withHour(14).withMinute(30), 
            designHead, List.of(personRepository.findByFirstNameAndLastName("Matthew", "Harris"),
                               personRepository.findByFirstNameAndLastName("Ashley", "Martin"))));
        
        meetings.add(createMeeting("Design Critique", "Critique session for ongoing design work", 
            now.plusDays(10).withHour(15).withMinute(0), now.plusDays(10).withHour(16).withMinute(0), 
            designHead, List.of(personRepository.findByFirstNameAndLastName("Ashley", "Martin"),
                               personRepository.findByFirstNameAndLastName("Matthew", "Harris"))));

        meetings.add(createMeeting("Sales Strategy Meeting", "Discuss Q1 sales strategy and targets", 
            now.plusDays(3).withHour(13).withMinute(0), now.plusDays(3).withHour(14).withMinute(0), 
            salesHead, List.of(personRepository.findByFirstNameAndLastName("Andrew", "Thompson"),
                               personRepository.findByFirstNameAndLastName("Nicole", "Garcia"))));
        
        meetings.add(createMeeting("Sales Pipeline Review", "Review sales pipeline and conversion rates", 
            now.plusDays(1).withHour(14).withMinute(0), now.plusDays(1).withHour(15).withMinute(0), 
            salesHead, List.of(personRepository.findByFirstNameAndLastName("Andrew", "Thompson"),
                               personRepository.findByFirstNameAndLastName("Nicole", "Garcia"))));
        
        meetings.add(createMeeting("Lead Generation Strategy", "Plan lead generation campaigns and tactics", 
            now.plusDays(5).withHour(10).withMinute(0), now.plusDays(5).withHour(11).withMinute(30), 
            salesHead, List.of(personRepository.findByFirstNameAndLastName("Nicole", "Garcia"),
                               personRepository.findByFirstNameAndLastName("Andrew", "Thompson"))));
        
        meetings.add(createMeeting("Customer Success Review", "Review customer success metrics and initiatives", 
            now.plusDays(6).withHour(16).withMinute(0), now.plusDays(6).withHour(17).withMinute(0), 
            salesHead, List.of(personRepository.findByFirstNameAndLastName("Andrew", "Thompson"),
                               personRepository.findByFirstNameAndLastName("Nicole", "Garcia"))));
        
        meetings.add(createMeeting("Sales Training Planning", "Plan sales training sessions and materials", 
            now.plusDays(8).withHour(11).withMinute(0), now.plusDays(8).withHour(12).withMinute(0), 
            salesHead, List.of(personRepository.findByFirstNameAndLastName("Nicole", "Garcia"),
                               personRepository.findByFirstNameAndLastName("Andrew", "Thompson"))));
        
        meetings.add(createMeeting("Territory Planning", "Plan and assign sales territories", 
            now.plusDays(9).withHour(14).withMinute(0), now.plusDays(9).withHour(15).withMinute(0), 
            salesHead, List.of(personRepository.findByFirstNameAndLastName("Andrew", "Thompson"),
                               personRepository.findByFirstNameAndLastName("Nicole", "Garcia"))));
        
        meetings.add(createMeeting("CRM Optimization", "Discuss CRM improvements and training", 
            now.plusDays(10).withHour(13).withMinute(0), now.plusDays(10).withHour(14).withMinute(0), 
            salesHead, List.of(personRepository.findByFirstNameAndLastName("Nicole", "Garcia"),
                               personRepository.findByFirstNameAndLastName("Andrew", "Thompson"))));

        meetings.add(createMeeting("HR Policy Review", "Review and update company HR policies", 
            now.plusDays(1).withHour(10).withMinute(0), now.plusDays(1).withHour(11).withMinute(0), 
            hrHead, List.of(personRepository.findByFirstNameAndLastName("Kevin", "Martinez"),
                           personRepository.findByFirstNameAndLastName("Lisa", "Robinson"))));
        
        meetings.add(createMeeting("Benefits Review", "Review employee benefits and compensation", 
            now.plusDays(4).withHour(14).withMinute(0), now.plusDays(4).withHour(15).withMinute(0), 
            hrHead, List.of(personRepository.findByFirstNameAndLastName("Kevin", "Martinez"),
                           personRepository.findByFirstNameAndLastName("Lisa", "Robinson"))));
        
        meetings.add(createMeeting("Recruitment Strategy", "Plan recruitment strategies and campaigns", 
            now.plusDays(6).withHour(11).withMinute(0), now.plusDays(6).withHour(12).withMinute(0), 
            hrHead, List.of(personRepository.findByFirstNameAndLastName("Lisa", "Robinson"),
                           personRepository.findByFirstNameAndLastName("Kevin", "Martinez"))));
        
        meetings.add(createMeeting("Employee Engagement", "Discuss employee engagement initiatives", 
            now.plusDays(7).withHour(15).withMinute(0), now.plusDays(7).withHour(16).withMinute(0), 
            hrHead, List.of(personRepository.findByFirstNameAndLastName("Kevin", "Martinez"),
                           personRepository.findByFirstNameAndLastName("Lisa", "Robinson"))));
        
        meetings.add(createMeeting("Training Program Development", "Plan employee training and development programs", 
            now.plusDays(9).withHour(10).withMinute(0), now.plusDays(9).withHour(11).withMinute(30), 
            hrHead, List.of(personRepository.findByFirstNameAndLastName("Lisa", "Robinson"),
                           personRepository.findByFirstNameAndLastName("Kevin", "Martinez"))));
        
        meetings.add(createMeeting("Workplace Safety Review", "Review workplace safety policies and procedures", 
            now.plusDays(10).withHour(14).withMinute(0), now.plusDays(10).withHour(15).withMinute(0), 
            hrHead, List.of(personRepository.findByFirstNameAndLastName("Kevin", "Martinez"),
                           personRepository.findByFirstNameAndLastName("Lisa", "Robinson"))));

        meetings.add(createCrossFunctionalMeeting("All Hands Meeting", "Company-wide update and announcements", 
            now.plusDays(5).withHour(16).withMinute(0), now.plusDays(5).withHour(17).withMinute(0), 
            hrHead, allPeople));
        
        meetings.add(createCrossFunctionalMeeting("Team Building Event", "Monthly team building and social event", 
            now.plusDays(7).withHour(17).withMinute(0), now.plusDays(7).withHour(19).withMinute(0), 
            hrHead, allPeople));
        
        meetings.add(createCrossFunctionalMeeting("Product-Engineering Sync", "Sync between product and engineering teams", 
            now.plusDays(2).withHour(16).withMinute(0), now.plusDays(2).withHour(17).withMinute(0), 
            productHead, List.of(personRepository.findByFirstNameAndLastName("Daniel", "Jackson"),
                               personRepository.findByFirstNameAndLastName("Jessica", "White"),
                               personRepository.findByFirstNameAndLastName("Michael", "Johnson"),
                               personRepository.findByFirstNameAndLastName("Jennifer", "Wilson"))));
        
        meetings.add(createCrossFunctionalMeeting("Design-Engineering Collaboration", "Collaboration session between design and engineering", 
            now.plusDays(4).withHour(13).withMinute(0), now.plusDays(4).withHour(14).withMinute(30), 
            designHead, List.of(personRepository.findByFirstNameAndLastName("Matthew", "Harris"),
                               personRepository.findByFirstNameAndLastName("Ashley", "Martin"),
                               personRepository.findByFirstNameAndLastName("Michael", "Johnson"),
                               personRepository.findByFirstNameAndLastName("Jennifer", "Wilson"))));
        
        meetings.add(createCrossFunctionalMeeting("Sales-Product Alignment", "Align sales strategy with product roadmap", 
            now.plusDays(6).withHour(10).withMinute(0), now.plusDays(6).withHour(11).withMinute(0), 
            salesHead, List.of(personRepository.findByFirstNameAndLastName("Andrew", "Thompson"),
                               personRepository.findByFirstNameAndLastName("Nicole", "Garcia"),
                               personRepository.findByFirstNameAndLastName("David", "Brown"),
                               personRepository.findByFirstNameAndLastName("Jessica", "White"))));
        
        meetings.add(createCrossFunctionalMeeting("Quarterly Planning", "Q1 planning session for all departments", 
            now.plusDays(8).withHour(9).withMinute(0), now.plusDays(8).withHour(12).withMinute(0), 
            hrHead, allPeople));
        
        meetings.add(createCrossFunctionalMeeting("Innovation Workshop", "Brainstorming session for new ideas and initiatives", 
            now.plusDays(11).withHour(14).withMinute(0), now.plusDays(11).withHour(16).withMinute(0), 
            productHead, allPeople));
        
        meetings.add(createCrossFunctionalMeeting("Client Demo Preparation", "Prepare for upcoming client demonstrations", 
            now.plusDays(3).withHour(16).withMinute(0), now.plusDays(3).withHour(17).withMinute(30), 
            salesHead, List.of(personRepository.findByFirstNameAndLastName("Andrew", "Thompson"),
                               personRepository.findByFirstNameAndLastName("Nicole", "Garcia"),
                               personRepository.findByFirstNameAndLastName("David", "Brown"),
                               personRepository.findByFirstNameAndLastName("Emily", "Davis"))));

        meetingRepository.saveAll(meetings);
    }

    private Meeting createMeeting(String title, String description, LocalDateTime startTime, LocalDateTime endTime, 
                                 Person organizer, List<Person> participants) {
        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setDepartment(organizer.getDepartment());
        meeting.setStartTime(startTime.toLocalTime());
        meeting.setEndTime(endTime.toLocalTime());
        meeting.setDay(startTime.toLocalDate());
        meeting.setOrganizer(organizer);
        meeting.setParticipants(new HashSet<>(participants));
        return meeting;
    }

    private Meeting createCrossFunctionalMeeting(String title, String description, LocalDateTime startTime, LocalDateTime endTime, 
                                               Person organizer, List<Person> participants) {
        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setDepartment(null);
        meeting.setStartTime(startTime.toLocalTime());
        meeting.setEndTime(endTime.toLocalTime());
        meeting.setDay(startTime.toLocalDate());
        meeting.setOrganizer(organizer);
        meeting.setParticipants(new HashSet<>(participants));
        return meeting;
    }
} 