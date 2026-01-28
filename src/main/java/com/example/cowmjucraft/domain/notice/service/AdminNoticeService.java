package com.example.cowmjucraft.domain.notice.service;

import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticeCreateRequestDto;
import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticeUpdateRequestDto;
import com.example.cowmjucraft.domain.notice.dto.response.NoticeDetailResponseDto;
import com.example.cowmjucraft.domain.notice.dto.response.NoticeSummaryResponseDto;
import com.example.cowmjucraft.domain.notice.entity.Notice;
import com.example.cowmjucraft.domain.notice.repository.NoticeRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;

    public AdminNoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Transactional
    public NoticeDetailResponseDto create(AdminNoticeCreateRequestDto request) {
        Notice notice = new Notice(request.title(), request.content(), request.imageKeys());
        Notice saved = noticeRepository.save(notice);
        return toDetailResponse(saved);
    }

    @Transactional
    public NoticeDetailResponseDto update(Long noticeId, AdminNoticeUpdateRequestDto request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "notice not found"));
        notice.update(request.title(), request.content(), request.imageKeys());
        return toDetailResponse(notice);
    }

    @Transactional
    public void delete(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "notice not found"));
        noticeRepository.delete(notice);
    }

    @Transactional(readOnly = true)
    public List<NoticeSummaryResponseDto> getNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoticeDetailResponseDto getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "notice not found"));
        return toDetailResponse(notice);
    }

    private NoticeSummaryResponseDto toSummaryResponse(Notice notice) {
        return new NoticeSummaryResponseDto(
                notice.getId(),
                notice.getTitle(),
                notice.getImageKeys(),
                notice.getCreatedAt()
        );
    }

    private NoticeDetailResponseDto toDetailResponse(Notice notice) {
        return new NoticeDetailResponseDto(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getImageKeys(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
