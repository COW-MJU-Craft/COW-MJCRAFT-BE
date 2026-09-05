package com.example.cowmjucraft.domain.order.dto.response;

public record AdminOrderExportResponseDto(
        String filename,
        byte[] content
) {
}
