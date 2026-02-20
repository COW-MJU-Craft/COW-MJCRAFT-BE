package com.example.cowmjucraft.domain.mypage.controller;

import com.example.cowmjucraft.domain.mypage.dto.request.MyPageAddressRequestDto;
import com.example.cowmjucraft.domain.mypage.dto.response.MyPageAddressResponseDto;
import com.example.cowmjucraft.domain.mypage.dto.response.MyPageResponseDto;
import com.example.cowmjucraft.domain.mypage.service.MyPageService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mypage")
public class MyPageController implements MyPageControllerDocs {

    private final MyPageService myPageService;

    @GetMapping
    @Override
    public ResponseEntity<ApiResult<MyPageResponseDto>> getMyPage(
            @AuthenticationPrincipal String memberId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, myPageService.getMyPage(parseMemberId(memberId)));
    }

    @PostMapping("/address")
    @Override
    public ResponseEntity<ApiResult<MyPageAddressResponseDto>> createAddress(
            @AuthenticationPrincipal String memberId,
            @Valid @RequestBody MyPageAddressRequestDto request
    ) {
        return ApiResponse.of(SuccessType.CREATED, myPageService.createAddress(parseMemberId(memberId), request));
    }

    @PutMapping("/address")
    @Override
    public ResponseEntity<ApiResult<MyPageAddressResponseDto>> updateAddress(
            @AuthenticationPrincipal String memberId,
            @Valid @RequestBody MyPageAddressRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, myPageService.updateAddress(parseMemberId(memberId), request));
    }

    @DeleteMapping("/address")
    @Override
    public ResponseEntity<ApiResult<Void>> deleteAddress(
            @AuthenticationPrincipal String memberId
    ) {
        myPageService.deleteAddress(parseMemberId(memberId));
        return ApiResponse.of(SuccessType.SUCCESS);
    }

    private Long parseMemberId(String principal) {
        try {
            return Long.valueOf(principal);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid member id");
        }
    }
}
