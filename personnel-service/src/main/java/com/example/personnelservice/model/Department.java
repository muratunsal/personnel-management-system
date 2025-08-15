package com.example.personnelservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String color;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "head_of_department_id")
    @JsonIgnoreProperties({"department"})
    private Person headOfDepartment;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Person getHeadOfDepartment() { return headOfDepartment; }
    public void setHeadOfDepartment(Person headOfDepartment) { this.headOfDepartment = headOfDepartment; }
} 