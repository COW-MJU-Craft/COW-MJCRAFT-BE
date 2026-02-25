package com.example.cowmjucraft.domain.payout.dto.request;

import com.example.cowmjucraft.domain.payout.entity.PayoutItemType;
import lombok.Getter;

@Getter
public class PayoutItemCreateAdminRequest {

    private PayoutItemType type;

    private String name;

    private long amount;

    private String category;
}
