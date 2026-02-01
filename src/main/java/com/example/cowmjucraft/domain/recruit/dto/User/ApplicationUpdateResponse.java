package com.example.cowmjucraft.domain.recruit.dto.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApplicationUpdateResponse {
    private Long applicationId;
    private LocalDateTime updatedAt;
}

