package com.example.personnelservice.repository;

import com.example.personnelservice.model.Task;
import com.example.personnelservice.model.Person;
import com.example.personnelservice.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByDepartmentOrderByCreatedAtDesc(Department department);
    List<Task> findByAssigneeOrderByCreatedAtDesc(Person assignee);
    List<Task> findByCreatedByOrderByCreatedAtDesc(Person createdBy);
}


