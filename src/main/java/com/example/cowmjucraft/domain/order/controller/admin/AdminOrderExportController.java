package com.example.cowmjucraft.domain.order.controller.admin;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderExportResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.service.AdminOrderExportService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminOrderExportController implements AdminOrderExportControllerDocs {

    private static final MediaType CSV_MEDIA_TYPE = MediaType.parseMediaType("text/csv;charset=UTF-8");

    private final AdminOrderExportService adminOrderExportService;

    @GetMapping(value = "/projects/{projectId}/orders/export", produces = "text/csv")
    @Override
    public ResponseEntity<byte[]> exportProjectOrders(
            @PathVariable Long projectId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderFulfillmentMethod fulfillmentMethod
    ) {
        AdminOrderExportResponseDto export = adminOrderExportService.exportProjectOrders(
                projectId,
                startDate,
                endDate,
                status,
                fulfillmentMethod
        );
        return fileResponse(export);
    }

    @GetMapping(value = "/orders/export", produces = "text/csv")
    @Override
    public ResponseEntity<byte[]> exportOrdersByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderFulfillmentMethod fulfillmentMethod
    ) {
        AdminOrderExportResponseDto export = adminOrderExportService.exportOrdersByDate(
                startDate,
                endDate,
                status,
                fulfillmentMethod
        );
        return fileResponse(export);
    }

    private ResponseEntity<byte[]> fileResponse(AdminOrderExportResponseDto export) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(CSV_MEDIA_TYPE);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(export.filename(), StandardCharsets.UTF_8)
                .build());
        headers.setCacheControl(CacheControl.noStore());
        return ResponseEntity.ok()
                .headers(headers)
                .body(export.content());
    }
}
