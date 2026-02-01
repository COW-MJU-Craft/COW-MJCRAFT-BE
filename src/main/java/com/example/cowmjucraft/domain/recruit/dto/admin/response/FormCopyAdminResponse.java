package com.example.cowmjucraft.domain.recruit.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FormCopyAdminResponse {
    private Long targetFormId;
    private Long sourceFormId;
    private int copiedCount;
}
