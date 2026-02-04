package com.example.cowmjucraft.domain.recruit.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerItem {
    private Long formQuestionId;
    private String value;
}
