package com.example.cowmjucraft.domain.introduce.service;

import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceUpsertRequest;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceResponse;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceDetailResponse;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceMainSummaryResponse;
import com.example.cowmjucraft.domain.introduce.entity.Introduce;
import com.example.cowmjucraft.domain.introduce.repository.IntroduceRepository;
import com.example.cowmjucraft.domain.introduce.section.IntroduceSectionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IntroduceService {

    private static final Long INTRODUCE_ID = 1L;

    private final IntroduceRepository introduceRepository;
    private final IntroduceJsonCodec jsonCodec;

    public IntroduceService(IntroduceRepository introduceRepository, ObjectMapper objectMapper) {
        this.introduceRepository = introduceRepository;
        this.jsonCodec = new IntroduceJsonCodec(objectMapper);
    }

    @Transactional(readOnly = true)
    public IntroduceMainSummaryResponse getMainSummary() {
        Introduce introduce = getIntroduceOrThrow();
        return new IntroduceMainSummaryResponse(
                introduce.getTitle(),
                introduce.getSubtitle(),
                introduce.getSummary(),
                jsonCodec.readHeroLogoKeys(introduce.getHeroLogoKeysJson())
        );
    }

    @Transactional(readOnly = true)
    public IntroduceDetailResponse getDetail() {
        Introduce introduce = getIntroduceOrThrow();
        return new IntroduceDetailResponse(jsonCodec.readSections(introduce.getSectionsJson()));
    }

    @Transactional(readOnly = true)
    public AdminIntroduceResponse adminGet() {
        Introduce introduce = getIntroduceOrThrow();
        return new AdminIntroduceResponse(
                introduce.getTitle(),
                introduce.getSubtitle(),
                introduce.getSummary(),
                jsonCodec.readHeroLogoKeys(introduce.getHeroLogoKeysJson()),
                jsonCodec.readSections(introduce.getSectionsJson()),
                introduce.getUpdatedAt()
        );
    }

    @Transactional
    public AdminIntroduceResponse adminUpsert(AdminIntroduceUpsertRequest request) {
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

        return new AdminIntroduceResponse(
                introduce.getTitle(),
                introduce.getSubtitle(),
                introduce.getSummary(),
                jsonCodec.readHeroLogoKeys(introduce.getHeroLogoKeysJson()),
                jsonCodec.readSections(introduce.getSectionsJson()),
                introduce.getUpdatedAt()
        );
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

            // TODO: 섹션별 필수 필드 검증은 여기서 확장
        }
    }

    private ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
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
}