package com.example.personnelservice.repository;

import com.example.personnelservice.model.TitleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TitleRepository extends JpaRepository<TitleEntity, Long> {
}


