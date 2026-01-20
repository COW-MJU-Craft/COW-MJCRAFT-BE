package com.example.cowmjucraft.domain.media.service;

import com.example.cowmjucraft.domain.media.dto.request.AdminMediaPresignRequestDto;
import com.example.cowmjucraft.domain.media.dto.response.AdminMediaPresignResponseDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaMetadataResponseDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignUrlResponseDto;
import com.example.cowmjucraft.domain.media.entity.Media;
import com.example.cowmjucraft.domain.media.entity.MediaStatus;
import com.example.cowmjucraft.domain.media.entity.MediaVisibility;
import com.example.cowmjucraft.domain.media.repository.MediaRepository;
import com.example.cowmjucraft.global.cloud.S3ObjectService;
import com.example.cowmjucraft.global.cloud.S3PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Transactional
@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final S3PresignService s3PresignService;
    private final S3ObjectService s3ObjectService;

    @Value("${aws.s3.presign-expire-seconds:300}")
    private long expireSeconds;

    public AdminMediaPresignResponseDto presignUpload(AdminMediaPresignRequestDto request) {
        if (request.contentType() == null || request.contentType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contentType is required");
        }
        if (request.usageType() == null || request.visibility() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usageType and visibility are required");
        }

        String originalFileName = (request.originalFileName() == null || request.originalFileName().isBlank())
                ? "file"
                : request.originalFileName();

        // TODO: presign 만료 시간이 S3PresignService와 MediaService에 분산됨; 만료 정책을 단일 설정으로 통합 고려.
        S3PresignService.PresignPutResult presign = s3PresignService.presignPut(
                originalFileName,
                request.contentType(),
                request.usageType()
        );

        return new AdminMediaPresignResponseDto(
                presign.key(),
                presign.uploadUrl(),
                expireSeconds
        );
    }

    @Transactional(readOnly = true)
    public MediaPresignUrlResponseDto getPublicPresignedUrl(Long mediaId) {
        Media media = getPublicActiveMedia(mediaId);
        S3PresignService.PresignGetResult presign = s3PresignService.presignGet(media.getKey());
        return new MediaPresignUrlResponseDto(presign.downloadUrl());
    }

    @Transactional(readOnly = true)
    public MediaMetadataResponseDto getPublicMetadata(Long mediaId) {
        Media media = getPublicActiveMedia(mediaId);
        return MediaMetadataResponseDto.from(media);
    }

    public void delete(Long mediaId) {
        Media media = getMediaOrThrow(mediaId);
        if (media.getStatus() == MediaStatus.DELETED) {
            return;
        }

        media.markDeleted();
        // TODO: S3 삭제 실패 시 DB 상태와 S3 실제 객체가 불일치할 수 있음; 재시도/비동기 처리로 안정성 개선 필요.
        s3ObjectService.delete(media.getKey());
    }

    private Media getPublicActiveMedia(Long mediaId) {
        Media media = getMediaOrThrow(mediaId);
        // TODO: ACTIVE + PUBLIC만 노출하고 나머지는 404로 숨겨 리소스 존재를 노출하지 않도록 함(보안 의도).
        if (media.getStatus() != MediaStatus.ACTIVE || media.getVisibility() != MediaVisibility.PUBLIC) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
        }
        return media;
    }

    private Media getMediaOrThrow(Long mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
    }
}
