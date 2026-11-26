package com.example.cowmjucraft.domain.analytics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "유입 채널별 방문 대비 주문 전환 응답")
public record AdminAnalyticsChannelsResponseDto(

        @Schema(description = "조회 시작일", example = "2026-11-01")
        String from,

        @Schema(description = "조회 종료일", example = "2026-11-26")
        String to,

        @Schema(description = "채널(source/medium/campaign)별 집계. 세션이 많은 순")
        List<Channel> channels,

        @Schema(description = "일별 방문 추이. 날짜 오름차순")
        List<Daily> daily
) {

    @Schema(description = "채널별 집계")
    public record Channel(

            @Schema(description = "유입 소스", example = "everytime.kr")
            String source,

            @Schema(description = "유입 매체", example = "referral")
            String medium,

            @Schema(description = "캠페인명. UTM이 없으면 (referral), (direct) 같은 GA 기본값", example = "(referral)")
            String campaign,

            @Schema(description = "세션 수", example = "159")
            long sessions,

            @Schema(description = "활성 사용자 수", example = "149")
            long users,

            @Schema(description = "상품 조회(view_item)를 한 사용자 수", example = "100")
            long viewItem,

            @Schema(description = "장바구니 담기(add_to_cart)를 한 사용자 수", example = "20")
            long addToCart,

            @Schema(description = "결제 시작(begin_checkout)을 한 사용자 수", example = "10")
            long beginCheckout,

            @Schema(description = "주문 제출(order_submit)을 한 사용자 수", example = "7")
            long orders
    ) {}

    @Schema(description = "일별 방문")
    public record Daily(

            @Schema(description = "날짜", example = "2026-11-25")
            String date,

            @Schema(description = "세션 수", example = "265")
            long sessions,

            @Schema(description = "활성 사용자 수", example = "220")
            long users
    ) {}
}
