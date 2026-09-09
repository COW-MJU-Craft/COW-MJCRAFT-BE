package com.example.cowmjucraft.domain.recruit.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"form_id", "studentId"})
        }
)
@Getter
@NoArgsConstructor
public class Application extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Form form;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepartmentType firstDepartment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepartmentType secondDepartment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultStatus resultStatus;

    @Version
    private Integer version;

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
        this.resultStatus = ResultStatus.NOT_PUBLISHED;
    }

    public void changeDepartments(DepartmentType firstDepartment, DepartmentType secondDepartment) {
        this.firstDepartment = firstDepartment;
        this.secondDepartment = secondDepartment;
    }

    public void setResultStatus(ResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }
}
