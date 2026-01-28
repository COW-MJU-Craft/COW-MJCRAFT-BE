package com.example.cowmjucraft.global.cloud;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.cowmjucraft.domain.media.policy.MediaUsageType;

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

    public String resolvePrefix(MediaUsageType usageType) {
        if (usageType == null) {
            return "uploads/etc/";
        }
        return switch (usageType) {
            case INTRO -> "uploads/intro/";
            case PROJECT -> "uploads/projects/";
            case LOGO -> "uploads/logos/";
            case JOURNAL -> "uploads/journals/";
            case NOTICE -> "uploads/notices/";
            case ETC -> "uploads/etc/";
        };
    }

    public int getExpireSeconds() {
        return Math.toIntExact(expireSeconds);
    }

    public record PresignPutResult(String key, String uploadUrl) {}
    public record PresignGetResult(String downloadUrl) {}
}
