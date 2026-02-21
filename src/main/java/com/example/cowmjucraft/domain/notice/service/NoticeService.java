package com.example.cowmjucraft.domain.notice.service;

import com.example.cowmjucraft.domain.notice.dto.response.NoticeDetailResponseDto;
import com.example.cowmjucraft.domain.notice.dto.response.NoticeSummaryResponseDto;
import com.example.cowmjucraft.domain.notice.entity.Notice;
import com.example.cowmjucraft.domain.notice.exception.NoticeErrorType;
import com.example.cowmjucraft.domain.notice.exception.NoticeException;
import com.example.cowmjucraft.domain.notice.repository.NoticeRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final S3PresignFacade s3PresignFacade;

    public NoticeService(
            NoticeRepository noticeRepository,
            S3PresignFacade s3PresignFacade
    ) {
        this.noticeRepository = noticeRepository;
        this.s3PresignFacade = s3PresignFacade;
    }

    @Transactional(readOnly = true)
    public List<NoticeSummaryResponseDto> getNotices() {
        List<Notice> notices = noticeRepository.findAllByOrderByCreatedAtDesc();
        Set<String> keySet = new LinkedHashSet<>();
        for (Notice notice : notices) {
            addIfValidKey(keySet, notice.getImageKeys());
        }
        Map<String, String> urls = presignGetSafely(keySet);

        return notices
                .stream()
                .map(notice -> toSummaryResponse(notice, urls))
                .toList();
    }

    @Transactional(readOnly = true)
    public NoticeDetailResponseDto getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(NoticeErrorType.NOTICE_NOT_FOUND));
        Set<String> keySet = new LinkedHashSet<>();
        addIfValidKey(keySet, notice.getImageKeys());
        Map<String, String> urls = presignGetSafely(keySet);
        return toDetailResponse(notice, urls);
    }

    private NoticeSummaryResponseDto toSummaryResponse(Notice notice, Map<String, String> urls) {
        return new NoticeSummaryResponseDto(
                notice.getId(),
                notice.getTitle(),
                notice.getImageKeys(),
                buildUrlsForKeys(notice.getImageKeys(), urls),
                notice.getCreatedAt()
        );
    }

    private NoticeDetailResponseDto toDetailResponse(Notice notice, Map<String, String> urls) {
        return new NoticeDetailResponseDto(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getImageKeys(),
                buildUrlsForKeys(notice.getImageKeys(), urls),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
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

    private List<String> buildUrlsForKeys(List<String> keys, Map<String, String> urls) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            String normalized = toNonBlankString(key);
            result.add(normalized == null ? null : urls.get(normalized));
        }
        return result;
    }

    private void addIfValidKey(Set<String> keys, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            String k = toNonBlankString(value);
            if (k != null) {
                keys.add(k);
            }
        }
    }

    private String toNonBlankString(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
