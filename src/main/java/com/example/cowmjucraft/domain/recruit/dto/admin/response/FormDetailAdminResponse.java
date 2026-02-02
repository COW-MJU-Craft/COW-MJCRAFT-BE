package com.example.cowmjucraft.domain.recruit.dto.admin.response;

public record FormDetailAdminResponse(
        Long formId,
        String title,
        boolean open
) {
}
