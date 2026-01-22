package com.example.cowmjucraft.domain.media.service;

import com.example.cowmjucraft.domain.media.dto.request.MediaDeleteRequestDto;
import com.example.cowmjucraft.domain.media.dto.request.MediaPresignGetRequestDto;
import com.example.cowmjucraft.domain.media.dto.request.MediaPresignPutRequestDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignGetResponseDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignPutResponseDto;
import com.example.cowmjucraft.global.cloud.S3ObjectService;
import com.example.cowmjucraft.global.cloud.S3PresignService;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Transactional
@Service
public class MediaPresignService {

    private final S3PresignService s3PresignService;
    private final S3ObjectService s3ObjectService;

    @Value("${aws.s3.presign-expire-seconds:300}")
    private long expireSeconds;

    public MediaPresignPutResponseDto presignPut(MediaPresignPutRequestDto request) {
        if (request.contentType() == null || request.contentType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contentType is required");
        }
        if (request.usageType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usageType is required");
        }

        String originalFileName = (request.originalFileName() == null || request.originalFileName().isBlank())
                ? "file"
                : request.originalFileName();

        S3PresignService.PresignPutResult presign = s3PresignService.presignPut(
                originalFileName,
                request.contentType(),
                request.usageType()
        );

        return new MediaPresignPutResponseDto(
                presign.key(),
                presign.uploadUrl(),
                expireSeconds
        );
    }

    @Transactional(readOnly = true)
    public MediaPresignGetResponseDto presignGet(MediaPresignGetRequestDto request) {
        if (request == null || request.keys() == null || request.keys().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keys are required");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keys are required");
        }

        request.keys().forEach(s3ObjectService::delete);
    }
}
