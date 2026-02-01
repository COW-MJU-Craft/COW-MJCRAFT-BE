package com.example.cowmjucraft.domain.recruit.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FormCreateAdminResponse {
    private Long formId;
    private boolean open;
}

