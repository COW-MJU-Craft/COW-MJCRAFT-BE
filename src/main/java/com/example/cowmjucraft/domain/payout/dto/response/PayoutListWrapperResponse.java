package com.example.cowmjucraft.domain.payout.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PayoutListWrapperResponse {

    private List<PayoutListResponse> payouts;
}
