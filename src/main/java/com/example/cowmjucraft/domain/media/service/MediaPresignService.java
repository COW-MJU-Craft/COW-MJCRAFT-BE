package com.example.cowmjucraft.domain.media.service;

import com.example.cowmjucraft.domain.media.dto.request.MediaDeleteRequestDto;
import com.example.cowmjucraft.domain.media.dto.request.MediaPresignGetRequestDto;
import com.example.cowmjucraft.domain.media.dto.request.MediaPresignPutRequestDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignGetResponseDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignPutResponseDto;
import com.example.cowmjucraft.domain.media.policy.MediaUsageType;
import com.example.cowmjucraft.global.cloud.S3ObjectService;
import com.example.cowmjucraft.global.cloud.S3PresignService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class MediaPresignService {

    private final S3PresignService s3PresignService;
    private final S3ObjectService s3ObjectService;

    public MediaPresignPutResponseDto createPresignPutBatch(MediaPresignPutRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 형식이 올바르지 않습니다. (request: null)");
        }
        if (request.files() == null || request.files().isEmpty()) {
            throw new IllegalArgumentException("요청 값 검증에 실패했습니다. (files: 비어 있을 수 없습니다.)");
        }

        String directory = normalizeDirectory(request.directory());
        List<MediaPresignPutResponseDto.ItemDto> items = new ArrayList<>();
        int expiresInSeconds = s3PresignService.getExpireSeconds();

        for (int i = 0; i < request.files().size(); i++) {
            MediaPresignPutRequestDto.FileDto file = request.files().get(i);
            if (file == null) {
                throw new IllegalArgumentException("요청 값 검증에 실패했습니다. (files[" + i + "]: null 항목이 포함되어 있습니다.)");
            }
            String safeName = sanitizeFileName(file.fileName(), i);
            if (file.contentType() == null || file.contentType().isBlank()) {
                throw new IllegalArgumentException("요청 값 검증에 실패했습니다. (files[" + i + "].contentType: 공백일 수 없습니다.)");
            }
            if (file.usageType() == null) {
                throw new IllegalArgumentException("요청 값 검증에 실패했습니다. (files[" + i + "].usageType: null일 수 없습니다.)");
            }

            String prefix = resolvePrefix(directory, file.usageType());
            String key = prefix + "/" + UUID.randomUUID() + "-" + safeName;
            S3PresignService.PresignPutResult presign = s3PresignService.presignPut(key, file.contentType());

            items.add(new MediaPresignPutResponseDto.ItemDto(
                    file.fileName(),
                    presign.key(),
                    presign.uploadUrl(),
                    expiresInSeconds
            ));
        }

        return new MediaPresignPutResponseDto(items);
    }

    @Transactional(readOnly = true)
    public MediaPresignGetResponseDto presignGet(MediaPresignGetRequestDto request) {
        if (request == null || request.keys() == null || request.keys().isEmpty()) {
            throw new IllegalArgumentException("요청 형식이 올바르지 않습니다. (keys: 비어 있을 수 없습니다.)");
        }

        Map<String, String> urls = new LinkedHashMap<>();
        for (String key : request.keys()) {
            urls.putIfAbsent(key, s3PresignService.presignGet(key).downloadUrl());
        }

        return new MediaPresignGetResponseDto(
                urls
        );
    }

    public void deleteByKeys(MediaDeleteRequestDto request) {
        if (request == null || request.keys() == null || request.keys().isEmpty()) {
            throw new IllegalArgumentException("요청 형식이 올바르지 않습니다. (keys: 비어 있을 수 없습니다.)");
        }

        request.keys().forEach(s3ObjectService::delete);
    }

    private String resolvePrefix(String directory, MediaUsageType usageType) {
        if (directory != null) {
            return directory;
        }
        return trimTrailingSlash(s3PresignService.resolvePrefix(usageType));
    }

    private String normalizeDirectory(String directory) {
        if (directory == null || directory.isBlank()) {
            return null;
        }
        return trimTrailingSlash(directory.trim());
    }

    private String trimTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String sanitizeFileName(String fileName, int index) {
        if (fileName == null) {
            throw new IllegalArgumentException("요청 값 검증에 실패했습니다. (files[" + index + "].fileName: 공백일 수 없습니다.)");
        }
        String trimmed = fileName.trim();
        String safeName = trimmed.replaceAll("\\s+", "_");
        if (safeName.isEmpty()) {
            throw new IllegalArgumentException("요청 값 검증에 실패했습니다. (files[" + index + "].fileName: 공백일 수 없습니다.)");
        }
        return safeName;
    }
}
