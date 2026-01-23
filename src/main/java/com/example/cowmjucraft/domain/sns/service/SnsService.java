package com.example.cowmjucraft.domain.sns.service;

import com.example.cowmjucraft.domain.sns.dto.request.SnsAdminRequestDto;
import com.example.cowmjucraft.domain.sns.dto.response.SnsResponseDto;
import com.example.cowmjucraft.domain.sns.entity.SnsLink;
import com.example.cowmjucraft.domain.sns.repository.SnsLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class SnsService {

    private final SnsLinkRepository snsLinkRepository;

    @Transactional(readOnly = true)
    public List<SnsResponseDto> getActiveSnsLinks() {
        return snsLinkRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(SnsResponseDto::from)
                .toList();
    }

    @Transactional
    public void replaceAll(List<SnsAdminRequestDto> requests) {
        List<SnsAdminRequestDto> safeRequests =
                Objects.requireNonNull(requests, "requests must not be null");

        snsLinkRepository.deleteAllInBatch();

        List<SnsLink> links = safeRequests.stream()
                .map(this::toEntity)
                .toList();

        if (!links.isEmpty()) {
            snsLinkRepository.saveAll(links);
        }
    }

    private SnsLink toEntity(SnsAdminRequestDto request) {
        return new SnsLink(
                request.type(),
                request.title(),
                request.url(),
                request.sortOrder(),
                request.active()
        );
    }
}