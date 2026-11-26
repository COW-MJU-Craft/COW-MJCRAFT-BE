package com.example.cowmjucraft.domain.analytics.controller.admin;

import com.example.cowmjucraft.domain.analytics.dto.response.AdminAnalyticsChannelsResponseDto;
import com.example.cowmjucraft.domain.analytics.service.AdminAnalyticsService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController implements AdminAnalyticsControllerDocs {

    private final AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/channels")
    @Override
    public ResponseEntity<ApiResult<AdminAnalyticsChannelsResponseDto>> getChannels(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminAnalyticsService.getChannels(from, to));
    }
}
