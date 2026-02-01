package com.example.cowmjucraft.domain.recruit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    private String description;

    public Question(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public void update(String label, String description) {
        this.label = label;
        this.description = description;
    }
}


