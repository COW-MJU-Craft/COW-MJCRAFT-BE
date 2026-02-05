package com.example.cowmjucraft.domain.recruit.dto.admin.response;

public record FormListAdminResponse(
        Long formId,
        String title,
        boolean open
) {
}
