package com.example.cowmjucraft.domain.recruit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SectionType sectionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private DepartmentType departmentType;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public FormNotice(Form form, SectionType sectionType, DepartmentType departmentType, String title, String content) {
        this.form = form;
        this.sectionType = sectionType;
        this.departmentType = departmentType;
        this.title = title;
        this.content = content;
    }

    public void update(SectionType sectionType, DepartmentType departmentType, String title, String content) {
        this.title = title;
        this.sectionType = sectionType;
        this.departmentType = departmentType;
        this.content = content;
    }
}