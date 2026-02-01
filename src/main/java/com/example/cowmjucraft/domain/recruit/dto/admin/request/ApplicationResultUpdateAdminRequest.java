package com.example.cowmjucraft.domain.recruit.dto.admin.request;

import com.example.cowmjucraft.domain.recruit.entity.ResultStatus;import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApplicationResultUpdateAdminRequest {
    private ResultStatus resultStatus;
}

