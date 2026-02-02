package com.example.cowmjucraft.domain.recruit.dto.client.response;

import java.time.LocalDateTime;

public record ApplicationUpdateResponse(
        Long applicationId,
        LocalDateTime updatedAt
) {
}
