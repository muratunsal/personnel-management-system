package com.example.personnelservice.dto;

import jakarta.validation.constraints.NotNull;

public class AssignHeadRequest {
    @NotNull
    private Long personId;

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }
}


