package com.example.cowmjucraft.domain.recruit.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FormListAdminResponse {

    private Long formId;
    private String title;
    private boolean open;
}

