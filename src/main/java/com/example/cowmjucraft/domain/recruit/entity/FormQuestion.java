package com.example.cowmjucraft.domain.recruit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "form_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Form form;

    @ManyToOne(optional = false)
    private Question question;

    @Column(nullable = false)
    private int questionOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerType answerType;

    @Column(nullable = false)
    private boolean required;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SectionType sectionType;

    @Enumerated(EnumType.STRING)
    @Column
    private DepartmentType departmentType;

    @Column(length = 2000)
    private String selectOptions;

    @Builder
    public FormQuestion(Form form, Question question, int questionOrder, AnswerType answerType, boolean required, SectionType sectionType, DepartmentType departmentType, String selectOptions) {
        this.form = form;
        this.question = question;
        this.questionOrder = questionOrder;
        this.answerType = answerType;
        this.required = required;
        this.sectionType = sectionType;
        this.departmentType = departmentType;
        this.selectOptions = selectOptions;
    }

    public void update(
            int questionOrder,
            boolean required,
            AnswerType answerType,
            String selectOptions,
            SectionType sectionType,
            DepartmentType departmentType
    ) {
        this.questionOrder = questionOrder;
        this.required = required;
        this.answerType = answerType;
        this.selectOptions = selectOptions;
        this.sectionType = sectionType;
        this.departmentType = departmentType;
    }
}
