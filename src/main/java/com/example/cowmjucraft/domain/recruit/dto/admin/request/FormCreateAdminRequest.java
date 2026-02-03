package com.example.cowmjucraft.domain.recruit.dto.admin.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FormCreateAdminRequest {
    private String title;
    private boolean open;
}
