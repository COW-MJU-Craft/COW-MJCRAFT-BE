package com.example.cowmjucraft.domain.recruit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "forms")
@Getter
@NoArgsConstructor
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private boolean open;

    public Form(String title, boolean open) {
        this.title = title;
        this.open = open;
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }

    public void changeTitle(String title) {
        this.title = title;
    }
}
