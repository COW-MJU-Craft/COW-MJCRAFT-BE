package com.example.cowmjucraft.domain.recruit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answers")
@Getter
@NoArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_question_id", nullable = false)
    private FormQuestion formQuestion;

    @Column(nullable = false, length = 2000)
    private String value;

    public Answer(Application application, FormQuestion formQuestion, String value) {
        this.application = application;
        this.formQuestion = formQuestion;
        this.value = value;
    }

    public void updateValue(String value) {
        this.value = value;
    }
}
