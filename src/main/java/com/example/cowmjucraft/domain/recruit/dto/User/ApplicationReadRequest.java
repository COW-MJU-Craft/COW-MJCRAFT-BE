package com.example.cowmjucraft.domain.recruit.dto.User;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApplicationReadRequest {
    private String studentId;
    private String password;
}
