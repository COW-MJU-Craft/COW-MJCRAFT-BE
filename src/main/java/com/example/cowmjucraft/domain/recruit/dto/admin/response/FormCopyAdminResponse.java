package com.example.cowmjucraft.domain.recruit.dto.admin.response;

public record FormCopyAdminResponse(
        Long targetFormId,
        Long sourceFormId,

        int copiedCount
) {
}
