package com.example.personnelservice.controller;

import com.example.personnelservice.model.TitleEntity;
import com.example.personnelservice.repository.TitleRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/titles")
@CrossOrigin(origins = "http://localhost:3000")
public class TitleController {
    private final TitleRepository titleRepository;

    public TitleController(TitleRepository titleRepository) {
        this.titleRepository = titleRepository;
    }

    @GetMapping
    public List<TitleEntity> all() {
        return titleRepository.findAll();
    }
} 