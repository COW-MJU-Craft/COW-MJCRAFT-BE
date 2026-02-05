package com.example.cowmjucraft.global.cloud;

import java.time.Duration;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3PresignService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.presign-expire-seconds:300}")
    private long expireSeconds;

    public PresignPutResult presignPut(String key, String contentType) {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expireSeconds))
                .putObjectRequest(putReq)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignReq);

        return new PresignPutResult(key, presigned.url().toString());
    }

    public PresignGetResult presignGet(String key) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expireSeconds))
                .getObjectRequest(getReq)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignReq);

        return new PresignGetResult(presigned.url().toString());
    }

    public PresignGetResult presignGet(
            String key,
            String downloadFileName,
            String contentType,
            boolean asAttachment
    ) {
        GetObjectRequest.Builder getReqBuilder = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key);

        String disposition = buildContentDisposition(downloadFileName, asAttachment);
        if (disposition != null) {
            getReqBuilder.responseContentDisposition(disposition);
        }
        if (contentType != null && !contentType.isBlank()) {
            getReqBuilder.responseContentType(contentType.trim());
        }

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expireSeconds))
                .getObjectRequest(getReqBuilder.build())
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignReq);

        return new PresignGetResult(presigned.url().toString());
    }

    public int getExpireSeconds() {
        return Math.toIntExact(expireSeconds);
    }

    private String buildContentDisposition(String fileName, boolean asAttachment) {
        String type = asAttachment ? "attachment" : "inline";
        String normalized = normalizeFileName(fileName);
        if (normalized == null) {
            return type;
        }
        String encoded = encodeUtf8FileName(normalized);
        String fallback = toAsciiFallbackFileName(normalized);
        return type + "; filename=\"" + fallback + "\"; filename*=UTF-8''" + encoded;
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        String trimmed = fileName.trim()
                .replace("/", "_")
                .replace("\\", "_");
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String encodeUtf8FileName(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return encoded.replace("+", "%20");
    }

    private String toAsciiFallbackFileName(String fileName) {
        String sanitized = fileName
                .replace("\r", "")
                .replace("\n", "")
                .replace("\"", "")
                .replaceAll("[^A-Za-z0-9._-]", "_");
        if (sanitized.isBlank()) {
            return "download";
        }
        return sanitized;
    }

    public record PresignPutResult(String key, String uploadUrl) {}
    public record PresignGetResult(String downloadUrl) {}
}
