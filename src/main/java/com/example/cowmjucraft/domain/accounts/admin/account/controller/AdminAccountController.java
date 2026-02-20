package com.example.cowmjucraft.domain.accounts.admin.account.controller;

import com.example.cowmjucraft.domain.accounts.admin.account.dto.request.AdminAccountUpdateRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.account.dto.response.AdminAccountResponseDto;
import com.example.cowmjucraft.domain.accounts.admin.account.service.AdminAccountService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminAccountController implements AdminAccountControllerDocs {

    private final AdminAccountService adminAccountService;

    @Override
    @PatchMapping("/account")
    public ResponseEntity<ApiResult<AdminAccountResponseDto>> updateAdminAccount(@Valid @RequestBody AdminAccountUpdateRequestDto request) {
        return ApiResponse.of(SuccessType.SUCCESS, adminAccountService.updateAdminAccount(request));
    }
}
