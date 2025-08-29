package com.example.personnelservice.repository;

import com.example.personnelservice.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    long countByHeadOfDepartmentId(Long headOfDepartmentId);
    java.util.List<com.example.personnelservice.model.Department> findByHeadOfDepartmentId(Long headOfDepartmentId);
}


