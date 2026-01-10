package com.example.cowmjucraft.domain.media.entity;

public enum MediaStatus {
    // TODO: presign 발급 후 업로드/도메인 연결이 완료되지 않은 상태를 의미; 일정 시간 이상 PENDING 유지 시 배치/비동기로 정리하는 정책 필요.
    PENDING,
    ACTIVE,
    DELETED
}
