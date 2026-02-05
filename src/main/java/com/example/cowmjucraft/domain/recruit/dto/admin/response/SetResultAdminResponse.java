package com.example.cowmjucraft.domain.recruit.dto.admin.response;

import com.example.cowmjucraft.domain.recruit.entity.ResultStatus;

public record SetResultAdminResponse(
        Long applicationId,
        ResultStatus resultStatus
) {
}
