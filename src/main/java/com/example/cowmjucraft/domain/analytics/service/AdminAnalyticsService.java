package com.example.cowmjucraft.domain.analytics.service;

import com.example.cowmjucraft.domain.analytics.config.GaProperties;
import com.example.cowmjucraft.domain.analytics.dto.response.AdminAnalyticsChannelsResponseDto;
import com.example.cowmjucraft.domain.analytics.exception.AnalyticsErrorType;
import com.example.cowmjucraft.domain.analytics.exception.AnalyticsException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * GA4 데이터를 채널별 방문 -> 주문 전환 형태로 집계한다.
 * GA API 호출 한도를 아끼고 화면을 빠르게 하려고 같은 기간 결과를 {@link GaProperties#getCacheTtl()} 동안 재사용한다.
 */
@Service
public class AdminAnalyticsService {

    static final int MAX_RANGE_DAYS = 366;
    private static final int MAX_CACHE_ENTRIES = 50;

    private static final String EVENT_VIEW_ITEM = "view_item";
    private static final String EVENT_ADD_TO_CART = "add_to_cart";
    private static final String EVENT_BEGIN_CHECKOUT = "begin_checkout";
    private static final String EVENT_ORDER = "order_submit";
    private static final List<String> FUNNEL_EVENTS =
            List.of(EVENT_VIEW_ITEM, EVENT_ADD_TO_CART, EVENT_BEGIN_CHECKOUT, EVENT_ORDER);

    private static final List<String> CHANNEL_DIMENSIONS =
            List.of("sessionSource", "sessionMedium", "sessionCampaignName");

    private final GaReportClient client;
    private final GaProperties properties;
    private final Clock clock;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Autowired
    public AdminAnalyticsService(GaReportClient client, GaProperties properties) {
        this(client, properties, Clock.systemUTC());
    }

    AdminAnalyticsService(GaReportClient client, GaProperties properties, Clock clock) {
        this.client = client;
        this.properties = properties;
        this.clock = clock;
    }

    public AdminAnalyticsChannelsResponseDto getChannels(LocalDate from, LocalDate to) {
        validateRange(from, to);
        if (!properties.isConfigured()) {
            throw new AnalyticsException(AnalyticsErrorType.NOT_CONFIGURED);
        }

        String key = from + "~" + to;
        Instant now = clock.instant();
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.expiresAt().isAfter(now)) {
            return cached.value();
        }

        AdminAnalyticsChannelsResponseDto fresh = load(from, to);
        evictIfNeeded(now);
        cache.put(key, new CacheEntry(now.plus(ttl()), fresh));
        return fresh;
    }

    private AdminAnalyticsChannelsResponseDto load(LocalDate from, LocalDate to) {
        List<GaReportClient.Row> sessionRows = client.runReport(new GaReportClient.Query(
                from, to, CHANNEL_DIMENSIONS, List.of("sessions", "activeUsers"), List.of()));

        List<String> eventDimensions = new java.util.ArrayList<>(CHANNEL_DIMENSIONS);
        eventDimensions.add("eventName");
        List<GaReportClient.Row> eventRows = client.runReport(new GaReportClient.Query(
                from, to, eventDimensions, List.of("totalUsers"), FUNNEL_EVENTS));

        List<GaReportClient.Row> dailyRows = client.runReport(new GaReportClient.Query(
                from, to, List.of("date"), List.of("sessions", "activeUsers"), List.of()));

        Map<String, Map<String, Long>> funnel = new HashMap<>();
        for (GaReportClient.Row row : eventRows) {
            String channelKey = channelKey(row);
            String eventName = row.dimensions().get(3);
            funnel.computeIfAbsent(channelKey, k -> new HashMap<>()).merge(eventName, number(row.metrics(), 0), Long::sum);
        }

        List<AdminAnalyticsChannelsResponseDto.Channel> channels = sessionRows.stream()
                .map(row -> {
                    Map<String, Long> events = funnel.getOrDefault(channelKey(row), Map.of());
                    return new AdminAnalyticsChannelsResponseDto.Channel(
                            row.dimensions().get(0),
                            row.dimensions().get(1),
                            row.dimensions().get(2),
                            number(row.metrics(), 0),
                            number(row.metrics(), 1),
                            events.getOrDefault(EVENT_VIEW_ITEM, 0L),
                            events.getOrDefault(EVENT_ADD_TO_CART, 0L),
                            events.getOrDefault(EVENT_BEGIN_CHECKOUT, 0L),
                            events.getOrDefault(EVENT_ORDER, 0L));
                })
                .sorted(Comparator.comparingLong(AdminAnalyticsChannelsResponseDto.Channel::sessions).reversed())
                .toList();

        List<AdminAnalyticsChannelsResponseDto.Daily> daily = dailyRows.stream()
                .map(row -> new AdminAnalyticsChannelsResponseDto.Daily(
                        formatDate(row.dimensions().get(0)),
                        number(row.metrics(), 0),
                        number(row.metrics(), 1)))
                .sorted(Comparator.comparing(AdminAnalyticsChannelsResponseDto.Daily::date))
                .toList();

        return new AdminAnalyticsChannelsResponseDto(from.toString(), to.toString(), channels, daily);
    }

    private static void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)
                || ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            throw new AnalyticsException(AnalyticsErrorType.INVALID_DATE_RANGE);
        }
    }

    private Duration ttl() {
        Duration ttl = properties.getCacheTtl();
        return ttl == null || ttl.isNegative() ? Duration.ZERO : ttl;
    }

    private void evictIfNeeded(Instant now) {
        if (cache.size() < MAX_CACHE_ENTRIES) {
            return;
        }
        cache.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        if (cache.size() >= MAX_CACHE_ENTRIES) {
            cache.clear();
        }
    }

    private static String channelKey(GaReportClient.Row row) {
        return String.join("\u0000", row.dimensions().get(0), row.dimensions().get(1), row.dimensions().get(2));
    }

    private static long number(List<String> metrics, int index) {
        if (index >= metrics.size()) {
            return 0L;
        }
        try {
            return Long.parseLong(metrics.get(index));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /** GA의 date 차원(yyyyMMdd)을 yyyy-MM-dd로 바꾼다. */
    private static String formatDate(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) {
            return yyyymmdd;
        }
        return yyyymmdd.substring(0, 4) + "-" + yyyymmdd.substring(4, 6) + "-" + yyyymmdd.substring(6);
    }

    private record CacheEntry(Instant expiresAt, AdminAnalyticsChannelsResponseDto value) {}
}
