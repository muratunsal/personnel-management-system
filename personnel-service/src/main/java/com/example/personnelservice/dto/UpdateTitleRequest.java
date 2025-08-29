package com.example.personnelservice.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public class UpdateTitleRequest {
    @Size(min = 1, max = 255)
    private String name;

    @NotNull
    private Long departmentId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}
