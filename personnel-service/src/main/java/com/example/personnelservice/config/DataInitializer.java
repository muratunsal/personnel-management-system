package com.example.personnelservice.config;

import com.example.personnelservice.model.Department;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.model.TitleEntity;
import com.example.personnelservice.repository.DepartmentRepository;
import com.example.personnelservice.repository.PersonRepository;
import com.example.personnelservice.repository.TitleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedPeople(PersonRepository personRepository, DepartmentRepository departmentRepository, TitleRepository titleRepository) {
        return args -> {
            if (personRepository.count() > 0) {
                log.info("People already seeded: {}", personRepository.count());
                return;
            }


            Map<String, String> departmentColors = new HashMap<>();
            departmentColors.put("Engineering", "#3b82f6");
            departmentColors.put("HR", "#14b8a6");
            departmentColors.put("Finance", "#f59e0b");
            departmentColors.put("Marketing", "#ef4444");
            departmentColors.put("Sales", "#8b5cf6");
            departmentColors.put("Support", "#10b981");
            departmentColors.put("Operations", "#6366f1");
            departmentColors.put("IT", "#06b6d4");
            departmentColors.put("Product", "#a3e635");
            departmentColors.put("Design", "#f472b6");
            departmentColors.put("Legal", "#f97316");

            Map<String, Department> nameToDept = new HashMap<>();
            for (Map.Entry<String, String> e : departmentColors.entrySet()) {
                Department d = new Department();
                d.setName(e.getKey());
                d.setColor(e.getValue());
                nameToDept.put(e.getKey(), departmentRepository.save(d));
            }


            String[] titleNames = new String[]{
                    "Software Engineer","HR Specialist","Accountant","Marketing Manager","Sales Executive",
                    "Support Engineer","DevOps Engineer","Operations Manager","System Administrator","Product Manager",
                    "UX Designer","QA Engineer","Legal Advisor","Frontend Developer","Backend Developer",
                    "Recruiter","Full Stack Developer","Financial Analyst","Customer Support","Account Manager","Data Engineer","Product Designer",
                    "Head of Engineering","Head of HR","Head of Finance","Head of Marketing","Head of Sales",
                    "Head of Support","Head of Operations","Head of IT","Head of Product","Head of Design","Head of Legal"
            };
            Map<String, TitleEntity> nameToTitle = new HashMap<>();
            for (String tName : titleNames) {
                TitleEntity t = new TitleEntity();
                t.setName(tName);
                nameToTitle.put(tName, titleRepository.save(t));
            }

            List<Person> people = new ArrayList<>();
            people.add(create("Cameron", "Williamson", "cameron.williamson@example.com", "+1 202 555 0001", "Engineering", "Head of Engineering", LocalDate.of(2015,1,1), LocalDate.of(1985,3,15), "Male", "New York, USA", null, nameToDept, nameToTitle));
            people.add(create("Leslie", "Alexander", "leslie.alexander@example.com", "+1 202 555 0002", "HR", "Head of HR", LocalDate.of(2016,2,1), LocalDate.of(1986,4,20), "Female", "Boston, USA", null, nameToDept, nameToTitle));
            people.add(create("Brooklyn", "Simmons", "brooklyn.simmons@example.com", "+1 202 555 0003", "Finance", "Head of Finance", LocalDate.of(2017,3,1), LocalDate.of(1987,5,25), "Female", "Chicago, USA", null, nameToDept, nameToTitle));
            people.add(create("Cody", "Fisher", "cody.fisher@example.com", "+1 202 555 0004", "Marketing", "Head of Marketing", LocalDate.of(2018,4,1), LocalDate.of(1988,6,30), "Male", "San Francisco, USA", null, nameToDept, nameToTitle));
            people.add(create("Ralph", "Edwards", "ralph.edwards@example.com", "+1 202 555 0005", "Sales", "Head of Sales", LocalDate.of(2019,5,1), LocalDate.of(1989,7,10), "Male", "Austin, USA", null, nameToDept, nameToTitle));
            people.add(create("Asther", "Mulvani", "asther.mulvani@example.com", "+1 202 555 0006", "Support", "Head of Support", LocalDate.of(2020,6,1), LocalDate.of(1990,8,15), "Female", "Seattle, USA", null, nameToDept, nameToTitle));
            people.add(create("Brooklyn", "Hehe", "brooklyn.hehe@example.com", "+1 202 555 0007", "Operations", "Head of Operations", LocalDate.of(2021,7,1), LocalDate.of(1991,9,20), "Female", "Portland, USA", null, nameToDept, nameToTitle));
            people.add(create("Jenny", "Wilson", "jenny.wilson@example.com", "+1 202 555 0008", "IT", "Head of IT", LocalDate.of(2022,8,1), LocalDate.of(1992,10,25), "Female", "Miami, USA", null, nameToDept, nameToTitle));
            people.add(create("Vidi", "Gutillerezz", "vidi.gutillerezz@example.com", "+1 202 555 0009", "Product", "Head of Product", LocalDate.of(2016,9,1), LocalDate.of(1986,11,5), "Male", "Los Angeles, USA", null, nameToDept, nameToTitle));
            people.add(create("Eden", "Khoiruddin", "eden.khoiruddin@example.com", "+1 202 555 0010", "Design", "Head of Design", LocalDate.of(2017,10,1), LocalDate.of(1987,12,10), "Male", "San Diego, USA", null, nameToDept, nameToTitle));
            people.add(create("Pablo", "Hive", "pablo.hive@example.com", "+1 202 555 0011", "Legal", "Head of Legal", LocalDate.of(2018,11,1), LocalDate.of(1988,1,15), "Male", "Phoenix, USA", null, nameToDept, nameToTitle));

            people.add(create("John", "Smith", "john.smith@example.com", "+1 202 555 0101", "Engineering", "Software Engineer", LocalDate.of(2020,3,1), LocalDate.of(1990,6,12), "Male", "New York, USA", null, nameToDept, nameToTitle));
            people.add(create("Emma", "Johnson", "emma.johnson@example.com", "+1 202 555 0102", "HR", "HR Specialist", LocalDate.of(2021,5,10), LocalDate.of(1992,1,23), "Female", "Boston, USA", null, nameToDept, nameToTitle));
            people.add(create("Liam", "Williams", "liam.williams@example.com", "+1 202 555 0103", "Finance", "Accountant", LocalDate.of(2019,9,15), LocalDate.of(1988,4,5), "Male", "Chicago, USA", null, nameToDept, nameToTitle));
            people.add(create("Olivia", "Brown", "olivia.brown@example.com", "+1 202 555 0104", "Marketing", "Marketing Manager", LocalDate.of(2022,2,1), LocalDate.of(1994,11,2), "Female", "San Francisco, USA", null, nameToDept, nameToTitle));
            people.add(create("Noah", "Jones", "noah.jones@example.com", "+1 202 555 0105", "Sales", "Sales Executive", LocalDate.of(2018,7,21), LocalDate.of(1989,9,18), "Male", "Austin, USA", null, nameToDept, nameToTitle));
            people.add(create("Ava", "Garcia", "ava.garcia@example.com", "+1 202 555 0106", "Support", "Support Engineer", LocalDate.of(2020,12,12), LocalDate.of(1993,8,30), "Female", "Seattle, USA", null, nameToDept, nameToTitle));
            people.add(create("William", "Miller", "william.miller@example.com", "+1 202 555 0107", "Engineering", "DevOps Engineer", LocalDate.of(2017,6,6), LocalDate.of(1987,5,22), "Male", "Denver, USA", null, nameToDept, nameToTitle));
            people.add(create("Sophia", "Davis", "sophia.davis@example.com", "+1 202 555 0108", "Operations", "Operations Manager", LocalDate.of(2016,10,10), LocalDate.of(1986,12,12), "Female", "Portland, USA", null, nameToDept, nameToTitle));
            people.add(create("James", "Rodriguez", "james.rodriguez@example.com", "+1 202 555 0109", "IT", "System Administrator", LocalDate.of(2015,3,19), LocalDate.of(1985,3,9), "Male", "Miami, USA", null, nameToDept, nameToTitle));
            people.add(create("Mia", "Martinez", "mia.martinez@example.com", "+1 202 555 0110", "Product", "Product Manager", LocalDate.of(2023,1,5), LocalDate.of(1996,2,14), "Female", "Los Angeles, USA", null, nameToDept, nameToTitle));
            people.add(create("Benjamin", "Hernandez", "benjamin.hernandez@example.com", "+1 202 555 0111", "Design", "UX Designer", LocalDate.of(2021,8,11), LocalDate.of(1991,7,19), "Male", "San Diego, USA", null, nameToDept, nameToTitle));
            people.add(create("Charlotte", "Lopez", "charlotte.lopez@example.com", "+1 202 555 0112", "Engineering", "QA Engineer", LocalDate.of(2019,11,30), LocalDate.of(1993,10,3), "Female", "Dallas, USA", null, nameToDept, nameToTitle));
            people.add(create("Elijah", "Gonzalez", "elijah.gonzalez@example.com", "+1 202 555 0113", "Legal", "Legal Advisor", LocalDate.of(2014,4,1), LocalDate.of(1984,1,1), "Male", "Phoenix, USA", null, nameToDept, nameToTitle));
            people.add(create("Amelia", "Wilson", "amelia.wilson@example.com", "+1 202 555 0114", "Engineering", "Frontend Developer", LocalDate.of(2020,9,9), LocalDate.of(1992,9,9), "Female", "Philadelphia, USA", null, nameToDept, nameToTitle));
            people.add(create("Lucas", "Anderson", "lucas.anderson@example.com", "+1 202 555 0115", "Engineering", "Backend Developer", LocalDate.of(2020,9,9), LocalDate.of(1991,1,21), "Male", "Atlanta, USA", null, nameToDept, nameToTitle));
            people.add(create("Harper", "Thomas", "harper.thomas@example.com", "+1 202 555 0116", "HR", "Recruiter", LocalDate.of(2022,5,20), LocalDate.of(1995,3,3), "Female", "Raleigh, USA", null, nameToDept, nameToTitle));
            people.add(create("Alexander", "Taylor", "alexander.taylor@example.com", "+1 202 555 0117", "Engineering", "Full Stack Developer", LocalDate.of(2018,2,2), LocalDate.of(1989,2,28), "Male", "Nashville, USA", null, nameToDept, nameToTitle));
            people.add(create("Evelyn", "Moore", "evelyn.moore@example.com", "+1 202 555 0118", "Finance", "Financial Analyst", LocalDate.of(2017,12,12), LocalDate.of(1988,7,7), "Female", "Columbus, USA", null, nameToDept, nameToTitle));
            people.add(create("Henry", "Jackson", "henry.jackson@example.com", "+1 202 555 0119", "Support", "Customer Support", LocalDate.of(2016,6,15), LocalDate.of(1987,6,6), "Male", "Salt Lake City, USA", null, nameToDept, nameToTitle));
            people.add(create("Abigail", "Martin", "abigail.martin@example.com", "+1 202 555 0120", "Sales", "Account Manager", LocalDate.of(2021,4,4), LocalDate.of(1993,4,4), "Female", "Orlando, USA", null, nameToDept, nameToTitle));
            people.add(create("Daniel", "Lee", "daniel.lee@example.com", "+1 202 555 0121", "Engineering", "Data Engineer", LocalDate.of(2019,3,3), LocalDate.of(1990,10,10), "Male", "San Jose, USA", null, nameToDept, nameToTitle));
            people.add(create("Avery", "Perez", "avery.perez@example.com", "+1 202 555 0122", "Product", "Product Designer", LocalDate.of(2022,10,1), LocalDate.of(1994,12,24), "Female", "Minneapolis, USA", null, nameToDept, nameToTitle));

            personRepository.saveAll(people);


            setHeadOfDepartment("Engineering", "Cameron", "Williamson", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("HR", "Leslie", "Alexander", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Finance", "Brooklyn", "Simmons", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Marketing", "Cody", "Fisher", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Sales", "Ralph", "Edwards", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Support", "Asther", "Mulvani", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Operations", "Brooklyn", "Hehe", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("IT", "Jenny", "Wilson", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Product", "Vidi", "Gutillerezz", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Design", "Eden", "Khoiruddin", nameToDept, personRepository, departmentRepository);
            setHeadOfDepartment("Legal", "Pablo", "Hive", nameToDept, personRepository, departmentRepository);

            log.info("Seeded {} people", personRepository.count());
        };
    }

    private Person create(String firstName, String lastName, String email, String phone, String department, String title,
                           LocalDate startDate, LocalDate birthDate, String gender, String address, String ppUrl,
                           Map<String, Department> nameToDept, Map<String, TitleEntity> nameToTitle) {
        Person p = new Person();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setEmail(email);
        p.setPhoneNumber(phone);
        p.setDepartment(nameToDept.get(department));
        p.setTitle(nameToTitle.get(title));
        p.setStartDate(startDate);
        p.setBirthDate(birthDate);
        p.setGender(gender);
        p.setAddress(address);
        p.setProfilePictureUrl(ppUrl);
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
} 