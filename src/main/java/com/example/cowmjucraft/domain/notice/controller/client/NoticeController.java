package com.example.cowmjucraft.domain.notice.controller.client;

import com.example.cowmjucraft.domain.notice.dto.response.NoticeDetailResponseDto;
import com.example.cowmjucraft.domain.notice.dto.response.NoticeSummaryResponseDto;
import com.example.cowmjucraft.domain.notice.service.NoticeService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notices")
public class NoticeController implements NoticeControllerDocs {

    private final NoticeService noticeService;

    @GetMapping
    @Override
    public ApiResult<List<NoticeSummaryResponseDto>> getNotices() {
        return ApiResult.success(SuccessType.SUCCESS, noticeService.getNotices());
    }

    @GetMapping("/{noticeId}")
    @Override
    public ApiResult<NoticeDetailResponseDto> getNotice(
            @PathVariable Long noticeId
    ) {
        return ApiResult.success(SuccessType.SUCCESS, noticeService.getNotice(noticeId));
    }
}
