package com.example.cowmjucraft.domain.introduce.service;

import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceCurrentLogoDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceDetailContentDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceIntroDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceLogoHistoryDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroducePurposeDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceDetailUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceMainUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroducePresignPutRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceDetailResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceMainResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroducePresignPutResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceBrandResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceCurrentLogoResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceDetailResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceLogoHistoryResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceMainSummaryResponseDto;
import com.example.cowmjucraft.domain.introduce.entity.Introduce;
import com.example.cowmjucraft.domain.introduce.repository.IntroduceRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class IntroduceService {

    private static final Long INTRODUCE_ID = 1L;

    private final IntroduceRepository introduceRepository;
    private final IntroduceJsonCodec jsonCodec;
    private final S3PresignFacade s3PresignFacade;

    public IntroduceService(
            IntroduceRepository introduceRepository,
            ObjectMapper objectMapper,
            S3PresignFacade s3PresignFacade
    ) {
        this.introduceRepository = introduceRepository;
        this.jsonCodec = new IntroduceJsonCodec(objectMapper);
        this.s3PresignFacade = s3PresignFacade;
    }

    @Transactional(readOnly = true)
    public IntroduceMainSummaryResponseDto getMainSummary() {
        Introduce introduce = getIntroduceOrThrow();
        return new IntroduceMainSummaryResponseDto(
                introduce.getTitle(),
                introduce.getSubtitle(),
                introduce.getSummary(),
                jsonCodec.readHeroLogoKeys(introduce.getHeroLogoKeysJson())
        );
    }

    @Transactional(readOnly = true)
    public IntroduceDetailResponseDto getDetail() {
        Introduce introduce = getIntroduceOrThrow();

        IntroduceDetailContentDto content = jsonCodec.readDetailContent(introduce.getSectionsJson());

        IntroduceIntroDto intro = applyIntroFallback(
                content == null ? null : content.intro(),
                introduce.getTitle(),
                introduce.getSubtitle()
        );

        IntroducePurposeDto purpose = safePurpose(content);
        IntroduceCurrentLogoDto currentLogo = safeCurrentLogo(content);
        List<IntroduceLogoHistoryDto> histories = safeHistories(content);

        Set<String> keys = collectPresignKeys(currentLogo, histories);
        Map<String, String> urls = presignGetSafely(keys);

        IntroduceCurrentLogoResponseDto currentLogoResponse = toCurrentLogoResponse(currentLogo, urls);
        List<IntroduceLogoHistoryResponseDto> historyResponses = toHistoryResponses(histories, urls);

        return new IntroduceDetailResponseDto(
                new IntroduceBrandResponseDto(introduce.getTitle(), introduce.getSubtitle()),
                intro,
                purpose,
                currentLogoResponse,
                historyResponses
        );
    }

    @Transactional(readOnly = true)
    public AdminIntroduceMainResponseDto adminGetMain() {
        Introduce introduce = getIntroduceOrThrow();
        return new AdminIntroduceMainResponseDto(
                introduce.getTitle(),
                introduce.getSubtitle(),
                introduce.getSummary(),
                jsonCodec.readHeroLogoKeys(introduce.getHeroLogoKeysJson()),
                introduce.getUpdatedAt()
        );
    }

    @Transactional
    public AdminIntroduceMainResponseDto adminUpsertMain(AdminIntroduceMainUpsertRequestDto request) {
        String heroLogoKeysJson = jsonCodec.writeJson(request.heroLogoKeys());

        Introduce introduce = introduceRepository.findById(INTRODUCE_ID).orElse(null);
        if (introduce == null) {
            String sectionsJson = jsonCodec.writeDetailContent(IntroduceDetailContentDto.empty());
            introduce = new Introduce(
                    request.title(),
                    request.subtitle(),
                    request.summary(),
                    heroLogoKeysJson,
                    sectionsJson
            );
        } else {
            introduce.update(
                    request.title(),
                    request.subtitle(),
                    request.summary(),
                    heroLogoKeysJson,
                    introduce.getSectionsJson()
            );
        }

        introduceRepository.save(introduce);

        return new AdminIntroduceMainResponseDto(
                introduce.getTitle(),
                introduce.getSubtitle(),
                introduce.getSummary(),
                jsonCodec.readHeroLogoKeys(introduce.getHeroLogoKeysJson()),
                introduce.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public AdminIntroduceDetailResponseDto adminGetDetail() {
        Introduce introduce = getIntroduceOrThrow();
        IntroduceDetailContentDto content = jsonCodec.readDetailContent(introduce.getSectionsJson());

        IntroduceIntroDto intro = content == null ? null : content.intro();
        IntroducePurposeDto purpose = safePurpose(content);
        IntroduceCurrentLogoDto currentLogo = safeCurrentLogo(content);
        List<IntroduceLogoHistoryDto> histories = safeHistories(content);

        return new AdminIntroduceDetailResponseDto(
                intro,
                purpose,
                currentLogo,
                histories,
                introduce.getUpdatedAt()
        );
    }

    @Transactional
    public AdminIntroduceDetailResponseDto adminUpsertDetail(AdminIntroduceDetailUpsertRequestDto request) {
        Introduce introduce = getIntroduceOrThrow();

        List<IntroduceLogoHistoryDto> histories = request.logoHistories() == null
                ? List.of()
                : normalizeLogoHistories(request.logoHistories());

        IntroduceDetailContentDto content = new IntroduceDetailContentDto(
                request.intro(),
                request.purpose(),
                request.currentLogo(),
                histories
        );

        String sectionsJson = jsonCodec.writeDetailContent(content);
        introduce.update(
                introduce.getTitle(),
                introduce.getSubtitle(),
                introduce.getSummary(),
                introduce.getHeroLogoKeysJson(),
                sectionsJson
        );
        introduceRepository.save(introduce);

        return new AdminIntroduceDetailResponseDto(
                content.intro(),
                content.purpose(),
                content.currentLogo(),
                content.logoHistories(),
                introduce.getUpdatedAt()
        );
    }

    public AdminIntroducePresignPutResponseDto createHeroLogoPresignPut(AdminIntroducePresignPutRequestDto request) {
        S3PresignFacade.PresignPutBatchResult response = s3PresignFacade.createPresignPutBatch(
                "uploads/introduce/hero-logos",
                List.of(new S3PresignFacade.PresignPutFile(request.fileName(), request.contentType()))
        );
        return toSinglePresignResponse(response);
    }

    public AdminIntroducePresignPutResponseDto createSectionPresignPut(AdminIntroducePresignPutRequestDto request) {
        S3PresignFacade.PresignPutBatchResult response = s3PresignFacade.createPresignPutBatch(
                "uploads/introduce/sections",
                List.of(new S3PresignFacade.PresignPutFile(request.fileName(), request.contentType()))
        );
        return toSinglePresignResponse(response);
    }

    private IntroducePurposeDto safePurpose(IntroduceDetailContentDto content) {
        if (content == null || content.purpose() == null) {
            return new IntroducePurposeDto(null, null);
        }
        return content.purpose();
    }

    private IntroduceCurrentLogoDto safeCurrentLogo(IntroduceDetailContentDto content) {
        if (content == null || content.currentLogo() == null) {
            return new IntroduceCurrentLogoDto(null, null, null);
        }
        return content.currentLogo();
    }

    private List<IntroduceLogoHistoryDto> safeHistories(IntroduceDetailContentDto content) {
        if (content == null || content.logoHistories() == null) {
            return List.of();
        }
        return normalizeLogoHistories(content.logoHistories());
    }

    private Introduce getIntroduceOrThrow() {
        return introduceRepository.findById(INTRODUCE_ID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "introduce not found"));
    }

    private List<IntroduceLogoHistoryDto> normalizeLogoHistories(List<IntroduceLogoHistoryDto> histories) {
        return histories == null ? List.of() : histories;
    }

    private IntroduceIntroDto applyIntroFallback(IntroduceIntroDto intro, String brandTitle, String brandSubtitle) {
        String title = intro == null ? null : intro.title();
        String slogan = intro == null ? null : intro.slogan();
        String body = intro == null ? null : intro.body();

        String resolvedTitle = isBlank(title) ? brandTitle : title;
        String resolvedSlogan = isBlank(slogan) ? brandSubtitle : slogan;

        return new IntroduceIntroDto(resolvedTitle, resolvedSlogan, body);
    }

    private Set<String> collectPresignKeys(IntroduceCurrentLogoDto currentLogo, List<IntroduceLogoHistoryDto> histories) {
        Set<String> keys = new LinkedHashSet<>();
        if (currentLogo != null) {
            addIfValidKey(keys, currentLogo.imageKey());
        }
        if (histories != null) {
            for (IntroduceLogoHistoryDto history : histories) {
                if (history == null) continue;
                addIfValidKey(keys, history.imageKey());
            }
        }
        return keys;
    }

    private Map<String, String> presignGetSafely(Set<String> keys) {
        try {
            return keys == null || keys.isEmpty()
                    ? Map.of()
                    : s3PresignFacade.presignGet(new ArrayList<>(keys));
        } catch (Exception e) {
            return Map.of();
        }
    }

    private IntroduceCurrentLogoResponseDto toCurrentLogoResponse(
            IntroduceCurrentLogoDto currentLogo,
            Map<String, String> urls
    ) {
        if (currentLogo == null) return null;

        String imageUrl = resolveUrl(urls, currentLogo.imageKey());

        return new IntroduceCurrentLogoResponseDto(
                currentLogo.title(),
                currentLogo.imageKey(),
                imageUrl,
                currentLogo.description()
        );
    }

    private List<IntroduceLogoHistoryResponseDto> toHistoryResponses(
            List<IntroduceLogoHistoryDto> histories,
            Map<String, String> urls
    ) {
        if (histories == null || histories.isEmpty()) return List.of();

        List<IntroduceLogoHistoryResponseDto> responses = new ArrayList<>();
        for (IntroduceLogoHistoryDto history : histories) {
            if (history == null) continue;

            responses.add(new IntroduceLogoHistoryResponseDto(
                    history.year(),
                    history.imageKey(),
                    resolveUrl(urls, history.imageKey()),
                    history.description()
            ));
        }
        return responses;
    }

    private String resolveUrl(Map<String, String> urls, String key) {
        if (urls == null || urls.isEmpty()) return null;

        String normalizedKey = toNonBlankString(key);
        return normalizedKey == null ? null : urls.get(normalizedKey);
    }

    private void addIfValidKey(Set<String> keys, String value) {
        if (keys == null) return;

        String key = toNonBlankString(value);
        if (key != null) keys.add(key);
    }

    private String toNonBlankString(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class IntroduceJsonCodec {

        private static final TypeReference<List<String>> HERO_LOGO_KEYS_TYPE = new TypeReference<>() {};
        private static final TypeReference<IntroduceDetailContentDto> DETAIL_CONTENT_TYPE = new TypeReference<>() {};
        private static final Logger log = LoggerFactory.getLogger(IntroduceJsonCodec.class);

        private final ObjectMapper objectMapper;

        private IntroduceJsonCodec(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        private List<String> readHeroLogoKeys(String json) {
            if (json == null) return null;
            try {
                return objectMapper.readValue(json, HERO_LOGO_KEYS_TYPE);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to parse heroLogoKeysJson", e);
            }
        }

        private IntroduceDetailContentDto readDetailContent(String json) {
            if (json == null) return IntroduceDetailContentDto.empty();
            try {
                IntroduceDetailContentDto content = objectMapper.readValue(json, DETAIL_CONTENT_TYPE);
                return content == null ? IntroduceDetailContentDto.empty() : content;
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse sectionsJson. Fallback to empty content.", e);
                return IntroduceDetailContentDto.empty();
            }
        }

        private String writeJson(Object value) {
            if (value == null) return null;
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to serialize introduce payload", e);
            }
        }

        private String writeDetailContent(IntroduceDetailContentDto content) {
            return writeJson(content);
        }
    }

    private AdminIntroducePresignPutResponseDto toSinglePresignResponse(S3PresignFacade.PresignPutBatchResult response) {
        if (response.items() == null || response.items().isEmpty()) {
            throw new IllegalArgumentException("presign items is empty");
        }
        S3PresignFacade.PresignPutItem item = response.items().get(0);
        return new AdminIntroducePresignPutResponseDto(
                item.key(),
                item.uploadUrl(),
                item.expiresInSeconds()
        );
    }
}