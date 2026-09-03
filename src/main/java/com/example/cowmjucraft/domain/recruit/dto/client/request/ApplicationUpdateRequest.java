package com.example.cowmjucraft.domain.recruit.dto.client.request;

import com.example.cowmjucraft.domain.recruit.entity.DepartmentType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ApplicationUpdateRequest {

    private Long formId;
    private String studentId;
    private String password;

    private DepartmentType firstDepartment;
    private DepartmentType secondDepartment;

    @Size(max = 100, message = "answers는 100개를 초과할 수 없습니다.")
    private List<AnswerItemRequest> answers;

    @Getter
    @NoArgsConstructor
    public static class AnswerItemRequest {
        private Long formQuestionId;
        private String value;
    }
}
