package com.example.cowmjucraft.domain.notice.controller.admin;

import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticeCreateRequestDto;
import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticePresignPutRequestDto;
import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticeUpdateRequestDto;
import com.example.cowmjucraft.domain.notice.dto.response.AdminNoticePresignPutResponseDto;
import com.example.cowmjucraft.domain.notice.dto.response.NoticeDetailResponseDto;
import com.example.cowmjucraft.domain.notice.dto.response.NoticeSummaryResponseDto;
import com.example.cowmjucraft.domain.notice.service.AdminNoticeService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
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
    public ApiResult<NoticeDetailResponseDto> createNotice(
            @Valid @RequestBody AdminNoticeCreateRequestDto request
    ) {
        return ApiResult.success(SuccessType.CREATED, adminNoticeService.create(request));
    }

    @PostMapping("/presign-put/images")
    @Override
    public ApiResult<AdminNoticePresignPutResponseDto> presignImage(
            @Valid @RequestBody AdminNoticePresignPutRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminNoticeService.createImagePresignPut(request));
    }

    @PutMapping("/{noticeId}")
    @Override
    public ApiResult<NoticeDetailResponseDto> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody AdminNoticeUpdateRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminNoticeService.update(noticeId, request));
    }

    @DeleteMapping("/{noticeId}")
    @Override
    public ApiResult<?> deleteNotice(
            @PathVariable Long noticeId
    ) {
        adminNoticeService.delete(noticeId);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @GetMapping
    @Override
    public ApiResult<List<NoticeSummaryResponseDto>> getNotices() {
        return ApiResult.success(SuccessType.SUCCESS, adminNoticeService.getNotices());
    }

    @GetMapping("/{noticeId}")
    @Override
    public ApiResult<NoticeDetailResponseDto> getNotice(
            @PathVariable Long noticeId
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminNoticeService.getNotice(noticeId));
    }
}
