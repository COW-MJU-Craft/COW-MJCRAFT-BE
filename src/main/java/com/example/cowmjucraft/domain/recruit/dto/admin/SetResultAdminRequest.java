package com.example.cowmjucraft.domain.recruit.dto.admin;

import com.example.cowmjucraft.domain.recruit.entity.ResultStatus;import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SetResultAdminRequest {
    private ResultStatus resultStatus;
}

