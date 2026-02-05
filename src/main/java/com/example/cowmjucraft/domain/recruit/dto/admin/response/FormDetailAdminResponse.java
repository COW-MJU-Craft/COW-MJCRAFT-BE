package com.example.cowmjucraft.domain.recruit.dto.admin.response;

import com.example.cowmjucraft.domain.recruit.entity.*;
import java.util.List;

public record FormDetailAdminResponse(
        Long formId,
        String title,
        boolean open,
        List<NoticeResponse> notices,
        List<QuestionResponse> questions
) {
    // 내부 record로 간단하게 정의
    public record NoticeResponse(
            Long noticeId,
            String sectionType,
            String departmentType,
            String title,
            String content
    ) {
        public static NoticeResponse from(FormNotice notice) {
            return new NoticeResponse(
                    notice.getId(),
                    notice.getSectionType().name(),
                    notice.getDepartmentType() != null ? notice.getDepartmentType().name() : null,
                    notice.getTitle(),
                    notice.getContent()
            );
        }
    }

    public record QuestionResponse(
            Long formQuestionId,
            int questionOrder,
            String content,
            String answerType,
            boolean required,
            String sectionType,
            String departmentType,
            String selectOptions
    ) {
        public static QuestionResponse from(FormQuestion fq) {
            return new QuestionResponse(
                    fq.getId(),
                    fq.getQuestionOrder(),
                    fq.getQuestion().getLabel(),
                    fq.getAnswerType().name(),
                    fq.isRequired(),
                    fq.getSectionType().name(),
                    fq.getDepartmentType() != null ? fq.getDepartmentType().name() : null,
                    fq.getSelectOptions()
            );
        }
    }
}