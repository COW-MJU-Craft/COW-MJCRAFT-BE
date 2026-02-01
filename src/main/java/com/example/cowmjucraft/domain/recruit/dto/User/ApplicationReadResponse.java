package com.example.cowmjucraft.domain.recruit.dto.User;

import com.example.cowmjucraft.domain.recruit.entity.DepartmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ApplicationReadResponse {

    private boolean editable;

    private Long applicationId;
    private String studentId;
    private DepartmentType firstDepartment;
    private DepartmentType secondDepartment;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    private List<AnswerItem> commonAnswers;
    private List<AnswerItem> firstDepartmentAnswers;
    private List<AnswerItem> secondDepartmentAnswers;

    @Getter
    @AllArgsConstructor
    public static class AnswerItem {
        private Long formQuestionId;
        private String value;
    }
}
