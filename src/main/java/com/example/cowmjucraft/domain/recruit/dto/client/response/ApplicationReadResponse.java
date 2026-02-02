package com.example.cowmjucraft.domain.recruit.dto.client.response;

import com.example.cowmjucraft.domain.recruit.entity.DepartmentType;
import java.time.LocalDateTime;
import java.util.List;

public record ApplicationReadResponse(
        boolean editable,
        Long applicationId,
        String studentId,
        DepartmentType firstDepartment,
        DepartmentType secondDepartment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AnswerItem> commonAnswers,
        List<AnswerItem> firstDepartmentAnswers,
        List<AnswerItem> secondDepartmentAnswers
) {
    public record AnswerItem(
            Long formQuestionId,
            String value
    ) {}
}
