package com.example.cowmjucraft.domain.recruit.dto.admin.response;

import com.example.cowmjucraft.domain.recruit.entity.ResultStatus;import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SetResultAdminResponse {
    private Long applicationId;
    private ResultStatus resultStatus;
}

