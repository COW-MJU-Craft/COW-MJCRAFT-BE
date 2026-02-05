package com.example.cowmjucraft.domain.recruit.dto.admin.response;

import com.example.cowmjucraft.domain.recruit.entity.AnswerType;
import com.example.cowmjucraft.domain.recruit.entity.DepartmentType;
import com.example.cowmjucraft.domain.recruit.entity.SectionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FormQuestionListAdminResponse {

    private Long formQuestionId;

    private Long questionId;
    private String label;
    private String description;

    private int questionOrder;
    private boolean required;

    private AnswerType answerType;
    private String selectOptions;

    private SectionType sectionType;
    private DepartmentType departmentType;
}
