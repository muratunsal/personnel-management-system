package com.example.personnelservice.dto;

import jakarta.validation.constraints.Size;

public class UpdateDepartmentRequest {
    @Size(min = 1, max = 255)
    private String name;

    @Size(min = 1, max = 7)
    private String color;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
