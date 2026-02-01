package com.example.cowmjucraft.domain.recruit.dto.user.response;

import com.example.cowmjucraft.domain.recruit.entity.ResultStatus;import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResultReadResponse {
    private ResultStatus resultStatus;
}
