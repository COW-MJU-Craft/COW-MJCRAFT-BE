package com.example.cowmjucraft.global.cloud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class S3PresignFacade {

    private final S3PresignService s3PresignService;
    private final S3ObjectService s3ObjectService;

    public PresignPutBatchResult createPresignPutBatch(String directory, List<PresignPutFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("요청 값 검증에 실패했습니다. (files: 비어 있을 수 없습니다.)");
        }

        String normalizedDirectory = normalizeDirectory(directory);
        if (normalizedDirectory == null) {
            throw new IllegalArgumentException("요청 값 검증에 실패했습니다. (directory: 공백일 수 없습니다.)");
        }

        List<PresignPutItem> items = new ArrayList<>();
        int expiresInSeconds = s3PresignService.getExpireSeconds();

        for (int i = 0; i < files.size(); i++) {
            PresignPutFile file = files.get(i);
            if (file == null) {
                throw new IllegalArgumentException("요청 값 검증에 실패했습니다. (files[" + i + "]: null 항목이 포함되어 있습니다.)");
            }
            String safeName = sanitizeFileName(file.fileName(), i);
            if (file.contentType() == null || file.contentType().isBlank()) {
                throw new IllegalArgumentException("요청 값 검증에 실패했습니다. (files[" + i + "].contentType: 공백일 수 없습니다.)");
            }

            String key = normalizedDirectory + "/" + UUID.randomUUID() + "-" + safeName;
            S3PresignService.PresignPutResult presign = s3PresignService.presignPut(key, file.contentType());

            items.add(new PresignPutItem(
                    file.fileName(),
                    presign.key(),
                    presign.uploadUrl(),
                    expiresInSeconds
            ));
        }

        return new PresignPutBatchResult(items);
    }

    public Map<String, String> presignGet(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("요청 형식이 올바르지 않습니다. (keys: 비어 있을 수 없습니다.)");
        }

        Map<String, String> urls = new LinkedHashMap<>();
        for (String key : keys) {
            urls.putIfAbsent(key, s3PresignService.presignGet(key).downloadUrl());
        }

        return urls;
    }

    public String presignGet(
            String key,
            String downloadFileName,
            String contentType,
            boolean asAttachment
    ) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("요청 형식이 올바르지 않습니다. (key: 공백일 수 없습니다.)");
        }
        return s3PresignService.presignGet(key.trim(), downloadFileName, contentType, asAttachment).downloadUrl();
    }

    public void deleteByKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("요청 형식이 올바르지 않습니다. (keys: 비어 있을 수 없습니다.)");
        }

        keys.forEach(s3ObjectService::delete);
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

    public record PresignPutFile(String fileName, String contentType) {
    }

    public record PresignPutItem(String fileName, String key, String uploadUrl, int expiresInSeconds) {
    }

    public record PresignPutBatchResult(List<PresignPutItem> items) {
    }
}
