package com.example.cowmjucraft.domain.recruit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"form_id", "studentId"})
        }
)
@Getter
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Form form;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private DepartmentType firstDepartment;

    @Column(nullable = false)
    private DepartmentType secondDepartment;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultStatus resultStatus;

    public Application(
            Form form,
            String studentId,
            String passwordHash,
            DepartmentType firstDepartment,
            DepartmentType secondDepartment
    ) {
        this.form = form;
        this.studentId = studentId;
        this.passwordHash = passwordHash;
        this.firstDepartment = firstDepartment;
        this.secondDepartment = secondDepartment;
        this.submittedAt = LocalDateTime.now();
        this.resultStatus = ResultStatus.NOT_PUBLISHED;
    }

    public void changeDepartments(DepartmentType firstDepartment, DepartmentType secondDepartment) {
        this.firstDepartment = firstDepartment;
        this.secondDepartment = secondDepartment;
    }

    public void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setResultStatus(ResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

}

