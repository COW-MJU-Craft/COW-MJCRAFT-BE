package com.example.cowmjucraft.domain.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cowmjucraft.domain.analytics.config.GaProperties;
import com.example.cowmjucraft.domain.analytics.dto.response.AdminAnalyticsChannelsResponseDto;
import com.example.cowmjucraft.domain.analytics.exception.AnalyticsErrorType;
import com.example.cowmjucraft.domain.analytics.exception.AnalyticsException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminAnalyticsServiceTest {

    private static final LocalDate FROM = LocalDate.of(2026, 11, 21);
    private static final LocalDate TO = LocalDate.of(2026, 11, 26);

    private GaReportClient client;
    private GaProperties properties;
    private MutableClock clock;
    private AdminAnalyticsService service;

    @BeforeEach
    void setUp() {
        client = mock(GaReportClient.class);
        properties = new GaProperties();
        properties.setPropertyId("123456");
        properties.setCredentialsBase64("e30=");
        properties.setCacheTtl(Duration.ofMinutes(15));
        clock = new MutableClock(Instant.parse("2026-11-26T00:00:00Z"));
        service = new AdminAnalyticsService(client, properties, clock);

        when(client.runReport(any())).thenAnswer(invocation -> {
            GaReportClient.Query query = invocation.getArgument(0);
            if (query.dimensions().contains("eventName")) {
                return List.of(
                        row(List.of("everytime.kr", "referral", "(referral)", "view_item"), "100"),
                        row(List.of("everytime.kr", "referral", "(referral)", "order_submit"), "7"),
                        row(List.of("google", "organic", "(organic)", "add_to_cart"), "33"));
            }
            if (query.dimensions().contains("date")) {
                return List.of(
                        new GaReportClient.Row(List.of("20261126"), List.of("15", "15")),
                        new GaReportClient.Row(List.of("20261125"), List.of("265", "220")));
            }
            return List.of(
                    new GaReportClient.Row(List.of("google", "organic", "(organic)"), List.of("38", "12")),
                    new GaReportClient.Row(List.of("everytime.kr", "referral", "(referral)"), List.of("159", "149")));
        });
    }

    @Test
    void 채널별로_세션과_퍼널_단계를_합치고_세션이_많은_순으로_정렬한다() {
        AdminAnalyticsChannelsResponseDto result = service.getChannels(FROM, TO);

        assertThat(result.from()).isEqualTo("2026-11-21");
        assertThat(result.to()).isEqualTo("2026-11-26");
        assertThat(result.channels()).hasSize(2);

        AdminAnalyticsChannelsResponseDto.Channel top = result.channels().get(0);
        assertThat(top.source()).isEqualTo("everytime.kr");
        assertThat(top.sessions()).isEqualTo(159);
        assertThat(top.users()).isEqualTo(149);
        assertThat(top.viewItem()).isEqualTo(100);
        assertThat(top.orders()).isEqualTo(7);
        assertThat(top.addToCart()).isZero();

        AdminAnalyticsChannelsResponseDto.Channel second = result.channels().get(1);
        assertThat(second.source()).isEqualTo("google");
        assertThat(second.addToCart()).isEqualTo(33);
        assertThat(second.orders()).isZero();
    }

    @Test
    void 일별_데이터는_날짜를_하이픈_형식으로_바꿔_오름차순으로_준다() {
        AdminAnalyticsChannelsResponseDto result = service.getChannels(FROM, TO);

        assertThat(result.daily()).extracting(AdminAnalyticsChannelsResponseDto.Daily::date)
                .containsExactly("2026-11-25", "2026-11-26");
        assertThat(result.daily().get(0).sessions()).isEqualTo(265);
    }

    @Test
    void 같은_기간은_캐시_시간_안에서_GA를_다시_호출하지_않는다() {
        service.getChannels(FROM, TO);
        service.getChannels(FROM, TO);

        verify(client, times(3)).runReport(any());

        clock.advance(Duration.ofMinutes(16));
        service.getChannels(FROM, TO);

        verify(client, times(6)).runReport(any());
    }

    @Test
    void 다른_기간은_별도로_조회한다() {
        service.getChannels(FROM, TO);
        service.getChannels(FROM, TO.minusDays(1));

        verify(client, times(6)).runReport(any());
    }

    @Test
    void 시작일이_종료일보다_늦으면_거절한다() {
        assertThatThrownBy(() -> service.getChannels(TO, FROM))
                .isInstanceOfSatisfying(AnalyticsException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(AnalyticsErrorType.INVALID_DATE_RANGE));
    }

    @Test
    void 조회_기간이_366일을_넘으면_거절한다() {
        assertThatThrownBy(() -> service.getChannels(LocalDate.of(2025, 11, 1), LocalDate.of(2026, 11, 26)))
                .isInstanceOfSatisfying(AnalyticsException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(AnalyticsErrorType.INVALID_DATE_RANGE));
    }

    @Test
    void 연동_설정이_없으면_503용_예외를_던진다() {
        properties.setCredentialsBase64("");

        assertThatThrownBy(() -> service.getChannels(FROM, TO))
                .isInstanceOfSatisfying(AnalyticsException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(AnalyticsErrorType.NOT_CONFIGURED));
    }

    private static GaReportClient.Row row(List<String> dimensions, String metric) {
        return new GaReportClient.Row(dimensions, List.of(metric));
    }

    private static class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
