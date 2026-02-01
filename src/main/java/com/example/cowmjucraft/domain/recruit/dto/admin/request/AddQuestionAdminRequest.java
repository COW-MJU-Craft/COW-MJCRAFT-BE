package com.example.cowmjucraft.domain.recruit.dto.admin.request;

import com.example.cowmjucraft.domain.recruit.entity.AnswerType;import com.example.cowmjucraft.domain.recruit.entity.DepartmentType;import com.example.cowmjucraft.domain.recruit.entity.SectionType;import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddQuestionAdminRequest {

    private String label;
    private String description;

    private int questionOrder;
    private boolean required;

    private AnswerType answerType;
    private String selectOptions;

    private SectionType sectionType;
    private DepartmentType departmentType;
}
