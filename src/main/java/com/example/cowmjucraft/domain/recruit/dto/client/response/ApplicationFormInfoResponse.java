package com.example.cowmjucraft.domain.recruit.dto.client.response;

import com.example.cowmjucraft.domain.recruit.entity.Form;
import com.example.cowmjucraft.domain.recruit.entity.FormNotice;
import com.example.cowmjucraft.domain.recruit.entity.FormQuestion;

import java.util.List;

public record ApplicationFormInfoResponse(
        Long formId,
        String title,
        List<NoticeDto> notices,
        List<QuestionDto> questions
) {

    public static ApplicationFormInfoResponse from(Form form, List<FormNotice> notices, List<FormQuestion> questions) {
        return new ApplicationFormInfoResponse(
                form.getId(),
                form.getTitle(),
                notices.stream().map(NoticeDto::from).toList(),
                questions.stream().map(QuestionDto::from).toList()
        );
    }

    public record NoticeDto(
            Long noticeId,
            String sectionType,
            String departmentType,
            String title,
            String content
    ) {
        public static NoticeDto from(FormNotice notice) {
            return new NoticeDto(
                    notice.getId(),
                    notice.getSectionType().name(),
                    notice.getDepartmentType() != null ? notice.getDepartmentType().name() : null,
                    notice.getTitle(),
                    notice.getContent()
            );
        }
    }

    public record QuestionDto(
            Long formQuestionId,
            int questionOrder,
            String content,
            String description,
            String answerType,
            boolean required,
            String sectionType,
            String departmentType,
            String selectOptions
    ) {
        public static QuestionDto from(FormQuestion fq) {
            return new QuestionDto(
                    fq.getId(),
                    fq.getQuestionOrder(),
                    fq.getQuestion().getLabel(),
                    fq.getQuestion().getDescription(),
                    fq.getAnswerType().name(),
                    fq.isRequired(),
                    fq.getSectionType().name(),
                    fq.getDepartmentType() != null ? fq.getDepartmentType().name() : null,
                    fq.getSelectOptions()
            );
        }
    }
}