package com.example.cowmjucraft.domain.sns.service;

import com.example.cowmjucraft.domain.sns.dto.request.SnsAdminRequestDto;
import com.example.cowmjucraft.domain.sns.dto.response.SnsResponseDto;
import com.example.cowmjucraft.domain.sns.entity.SnsLink;
import com.example.cowmjucraft.domain.sns.entity.SnsType;
import com.example.cowmjucraft.domain.sns.repository.SnsLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class SnsService {

    private final SnsLinkRepository snsLinkRepository;

    @Transactional(readOnly = true)
    public SnsResponseDto getLink(SnsType type) {
        SnsLink link = snsLinkRepository.findByType(type)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "sns link not found: " + type
                ));
        return SnsResponseDto.from(link);
    }

    @Transactional
    public void upsert(SnsType type, SnsAdminRequestDto request) {
        SnsAdminRequestDto safeRequest = Objects.requireNonNull(request, "request must not be null");

        SnsLink link = snsLinkRepository.findByType(type)
                .orElse(null);

        if (link == null) {
            snsLinkRepository.save(new SnsLink(type, safeRequest.url()));
            return;
        }

        link.updateUrl(safeRequest.url());
    }
}
