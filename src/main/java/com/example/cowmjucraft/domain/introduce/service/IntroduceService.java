package com.example.cowmjucraft.domain.introduce.service;

import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroducePresignPutRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroducePresignPutResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceDetailResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceMainSummaryResponseDto;
import com.example.cowmjucraft.domain.introduce.entity.Introduce;
import com.example.cowmjucraft.domain.introduce.repository.IntroduceRepository;
import com.example.cowmjucraft.domain.introduce.section.IntroduceSectionType;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        List<Map<String, Object>> sections =
                new ArrayList<>(jsonCodec.readSections(introduce.getSectionsJson()));

        Set<String> keys = collectPresignKeys(sections);

        Map<String, String> urls;
        try {
            urls = keys.isEmpty()
                    ? Map.of()
                    : s3PresignFacade.presignGet(new ArrayList<>(keys));
        } catch (Exception e) {
            urls = Map.of();
        }

        injectPresignedUrls(sections, urls);
        return new IntroduceDetailResponseDto(sections);
    }

    @Transactional(readOnly = true)
    public AdminIntroduceResponseDto adminGet() {
        Introduce introduce = getIntroduceOrThrow();
        return new AdminIntroduceResponseDto(
                introduce.getTitle(),
                introduce.getSubtitle(),
                introduce.getSummary(),
                jsonCodec.readHeroLogoKeys(introduce.getHeroLogoKeysJson()),
                jsonCodec.readSections(introduce.getSectionsJson()),
                introduce.getUpdatedAt()
        );
    }

    @Transactional
    public AdminIntroduceResponseDto adminUpsert(AdminIntroduceUpsertRequestDto request) {
        validateSections(request.sections());

        String heroLogoKeysJson = jsonCodec.writeJson(request.heroLogoKeys());
        String sectionsJson = jsonCodec.writeJson(request.sections());

        Introduce introduce = introduceRepository.findById(INTRODUCE_ID)
                .orElseGet(() -> new Introduce(
                        request.title(),
                        request.subtitle(),
                        request.summary(),
                        heroLogoKeysJson,
                        sectionsJson
                ));

        introduce.update(
                request.title(),
                request.subtitle(),
                request.summary(),
                heroLogoKeysJson,
                sectionsJson
        );

        introduceRepository.save(introduce);

        return new AdminIntroduceResponseDto(
                introduce.getTitle(),
                introduce.getSubtitle(),
                introduce.getSummary(),
                jsonCodec.readHeroLogoKeys(introduce.getHeroLogoKeysJson()),
                jsonCodec.readSections(introduce.getSectionsJson()),
                introduce.getUpdatedAt()
        );
    }

    public AdminIntroducePresignPutResponseDto createHeroLogoPresignPut(
            AdminIntroducePresignPutRequestDto request
    ) {
        S3PresignFacade.PresignPutBatchResult response = s3PresignFacade.createPresignPutBatch(
                "uploads/introduce/hero-logos",
                List.of(new S3PresignFacade.PresignPutFile(request.fileName(), request.contentType()))
        );
        return toSinglePresignResponse(response);
    }

    public AdminIntroducePresignPutResponseDto createSectionPresignPut(
            AdminIntroducePresignPutRequestDto request
    ) {
        S3PresignFacade.PresignPutBatchResult response = s3PresignFacade.createPresignPutBatch(
                "uploads/introduce/sections",
                List.of(new S3PresignFacade.PresignPutFile(request.fileName(), request.contentType()))
        );
        return toSinglePresignResponse(response);
    }

    private Introduce getIntroduceOrThrow() {
        return introduceRepository.findById(INTRODUCE_ID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "introduce not found"));
    }

    private void validateSections(List<Map<String, Object>> sections) {
        if (sections == null || sections.isEmpty()) {
            throw badRequest("sections are required");
        }

        for (int i = 0; i < sections.size(); i++) {
            Map<String, Object> section = sections.get(i);

            Object typeObj = section.get("type");
            if (!(typeObj instanceof String)) {
                throw badRequest(errTypeRequired(i));
            }

            String typeText = ((String) typeObj).trim();
            if (typeText.isEmpty()) {
                throw badRequest(errTypeRequired(i));
            }

            try {
                IntroduceSectionType.valueOf(typeText);
            } catch (IllegalArgumentException e) {
                throw badRequest(errInvalidType(i, typeText));
            }

        }
    }

    private ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private Set<String> collectPresignKeys(List<Map<String, Object>> sections) {
        if (sections == null || sections.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<String> keys = new LinkedHashSet<>();
        for (Map<String, Object> section : sections) {
            if (section == null) {
                continue;
            }
            String type = toNonBlankString(section.get("type"));
            if (type == null) {
                continue;
            }

            if ("CURRENT_LOGO".equals(type)) {
                addIfValidKey(keys, section.get("logoImageKey"));
                continue;
            }
            if ("LOGO_HISTORY".equals(type)) {
                Object historiesObj = section.get("histories");
                if (historiesObj instanceof List<?> histories) {
                    for (Object itemObj : histories) {
                        if (!(itemObj instanceof Map<?, ?> itemMap)) {
                            continue;
                        }
                        addIfValidKey(keys, itemMap.get("imageKey"));
                    }
                }
                continue;
            }
            if ("HEADER".equals(type)) {
                addIfValidKey(keys, section.get("backgroundImageKey"));
            }
        }
        return keys;
    }

    private void injectPresignedUrls(List<Map<String, Object>> sections, Map<String, String> urls) {
        if (sections == null || sections.isEmpty() || urls == null || urls.isEmpty()) {
            return;
        }

        for (Map<String, Object> section : sections) {
            if (section == null) {
                continue;
            }
            String type = toNonBlankString(section.get("type"));
            if (type == null) {
                continue;
            }

            if ("CURRENT_LOGO".equals(type)) {
                String logoImageKey = toNonBlankString(section.get("logoImageKey"));
                if (logoImageKey != null) {
                    String url = urls.get(logoImageKey);
                    if (url != null) {
                        section.put("logoImageUrl", url);
                    }
                }
                continue;
            }
            if ("LOGO_HISTORY".equals(type)) {
                Object historiesObj = section.get("histories");
                if (historiesObj instanceof List<?> histories) {
                    for (Object itemObj : histories) {
                        if (!(itemObj instanceof Map<?, ?> itemMap)) {
                            continue;
                        }
                        String imageKey = toNonBlankString(itemMap.get("imageKey"));
                        if (imageKey == null) {
                            continue;
                        }
                        String url = urls.get(imageKey);
                        if (url != null) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> typedMap = (Map<String, Object>) itemMap;
                            typedMap.put("imageUrl", url);
                        }
                    }
                }
                continue;
            }
            if ("HEADER".equals(type)) {
                String backgroundImageKey = toNonBlankString(section.get("backgroundImageKey"));
                if (backgroundImageKey != null) {
                    String url = urls.get(backgroundImageKey);
                    if (url != null) {
                        section.put("backgroundImageUrl", url);
                    }
                }
            }
        }
    }

    private void addIfValidKey(Set<String> keys, Object value) {
        if (keys == null) {
            return;
        }
        String key = toNonBlankString(value);
        if (key != null) {
            keys.add(key);
        }
    }

    private String toNonBlankString(Object value) {
        if (!(value instanceof String text)) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String errTypeRequired(int idx) {
        return "sections[" + idx + "].type is required (allowed: " + allowedTypes() + ")";
    }

    private String errInvalidType(int idx, String value) {
        return "sections[" + idx + "].type is invalid: " + value + " (allowed: " + allowedTypes() + ")";
    }

    private String allowedTypes() {
        return Arrays.stream(IntroduceSectionType.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    private static final class IntroduceJsonCodec {

        private static final TypeReference<List<String>> HERO_LOGO_KEYS_TYPE = new TypeReference<>() {};
        private static final TypeReference<List<Map<String, Object>>> SECTIONS_TYPE = new TypeReference<>() {};

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

        private List<Map<String, Object>> readSections(String json) {
            if (json == null) return List.of();
            try {
                return objectMapper.readValue(json, SECTIONS_TYPE);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to parse sectionsJson", e);
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
