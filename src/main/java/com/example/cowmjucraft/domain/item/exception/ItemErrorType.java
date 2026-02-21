package com.example.cowmjucraft.domain.item.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemErrorType implements ErrorCode {

    PROJECT_NOT_FOUND(404, "프로젝트를 찾을 수 없습니다."),
    ITEM_NOT_FOUND(404, "상품을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(404, "이미지를 찾을 수 없습니다."),

    SORT_ORDER_CONFLICT(409, "이미 사용 중인 정렬 순서입니다."),
    IMAGE_NOT_BELONG_TO_ITEM(409, "해당 상품의 이미지가 아닙니다."),

    ITEM_TYPE_MISMATCH(422, "상품 유형이 프로젝트 카테고리와 맞지 않습니다."),
    DIGITAL_JOURNAL_VIOLATION(422, "디지털 저널 상품 규칙을 위반했습니다."),
    GROUPBUY_VIOLATION(422, "공동구매 상품 규칙을 위반했습니다."),
    NORMAL_SALE_VIOLATION(422, "일반 판매 상품 규칙을 위반했습니다."),
    THUMBNAIL_REQUIRED(422, "썸네일 키가 필요합니다."),
    DESCRIPTION_REQUIRED(422, "상품 설명이 필요합니다."),
    JOURNAL_FILE_KEY_REQUIRED(422, "저널 파일 키가 필요합니다."),
    IMAGE_REQUIRED(422, "이미지 목록이 필요합니다."),
    FILE_REQUIRED(422, "파일 목록이 필요합니다."),
    SORT_ORDER_INVALID(422, "정렬 순서가 올바르지 않습니다."),
    IMAGE_SIZE_MISMATCH(422, "이미지 수가 일치하지 않습니다."),
    PROJECT_CATEGORY_MISMATCH(422, "프로젝트 카테고리와 맞지 않는 요청입니다."),

    S3_DELETE_FAILED(500, "S3 파일 삭제에 실패했습니다."),
    PRESIGN_FAILED(500, "업로드/다운로드 URL 생성에 실패했습니다.");

    private final int httpStatusCode;
    private final String message;
}
