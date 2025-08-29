package com.example.personnelservice.repository;

import com.example.personnelservice.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
    Person findByFirstNameAndLastName(String firstName, String lastName);
    Person findByEmail(String email);
    long countByDepartmentId(Long departmentId);
    long countByTitleId(Long titleId);
    java.util.List<Person> findByDepartmentId(Long departmentId);
    java.util.List<Person> findByTitleId(Long titleId);
} 