package com.example.personnelservice.repository;

import com.example.personnelservice.model.Title;
import com.example.personnelservice.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TitleRepository extends JpaRepository<Title, Long> {
    List<Title> findByDepartment(Department department);
    List<Title> findByDepartmentId(Long departmentId);
    Optional<Title> findByDepartmentIdAndNameIgnoreCase(Long departmentId, String name);
    boolean existsByNameIgnoreCase(String name);
}


