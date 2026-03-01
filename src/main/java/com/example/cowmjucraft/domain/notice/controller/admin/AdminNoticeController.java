package com.example.cowmjucraft.domain.notice.controller.admin;

import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticeCreateRequestDto;
import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticePresignPutRequestDto;
import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticeUpdateRequestDto;
import com.example.cowmjucraft.domain.notice.dto.response.AdminNoticeDetailResponseDto;
import com.example.cowmjucraft.domain.notice.dto.response.AdminNoticePresignPutResponseDto;
import com.example.cowmjucraft.domain.notice.dto.response.AdminNoticeSummaryResponseDto;
import com.example.cowmjucraft.domain.notice.service.AdminNoticeService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/notices")
public class AdminNoticeController implements AdminNoticeControllerDocs {

    private final AdminNoticeService adminNoticeService;

    @PostMapping
    @Override
    public ResponseEntity<ApiResult<AdminNoticeDetailResponseDto>> createNotice(
            @Valid @RequestBody AdminNoticeCreateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.CREATED, adminNoticeService.create(request));
    }

    @PostMapping("/presign-put/images")
    @Override
    public ResponseEntity<ApiResult<AdminNoticePresignPutResponseDto>> presignImage(
            @Valid @RequestBody AdminNoticePresignPutRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminNoticeService.createImagePresignPut(request));
    }

    @PutMapping("/{noticeId}")
    @Override
    public ResponseEntity<ApiResult<AdminNoticeDetailResponseDto>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody AdminNoticeUpdateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminNoticeService.update(noticeId, request));
    }

    @DeleteMapping("/{noticeId}")
    @Override
    public ResponseEntity<ApiResult<Void>> deleteNotice(
            @PathVariable Long noticeId
    ) {
        adminNoticeService.delete(noticeId);
        return ApiResponse.of(SuccessType.SUCCESS);
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResult<List<AdminNoticeSummaryResponseDto>>> getNotices() {
        return ApiResponse.of(SuccessType.SUCCESS, adminNoticeService.getNotices());
    }

    @GetMapping("/{noticeId}")
    @Override
    public ResponseEntity<ApiResult<AdminNoticeDetailResponseDto>> getNotice(
            @PathVariable Long noticeId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminNoticeService.getNotice(noticeId));
    }
}
