package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderExportResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.exception.ProjectErrorType;
import com.example.cowmjucraft.domain.project.exception.ProjectException;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderExportService {

    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String ITEM_SEPARATOR = " | ";
    private static final String[] CSV_HEADERS = {
            "주문일자",
            "주문번호",
            "주문상태",
            "이름",
            "학과",
            "학번",
            "연락처",
            "총액",
            "주문상품",
            "상품별 수량",
            "수령방식",
            "주소",
            "환불은행",
            "환불계좌"
    };

    private final ProjectRepository projectRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final OrderFulfillmentRepository orderFulfillmentRepository;

    @Transactional(readOnly = true)
    public AdminOrderExportResponseDto exportProjectOrders(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            OrderStatus status,
            OrderFulfillmentMethod fulfillmentMethod
    ) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(
                        ProjectErrorType.PROJECT_NOT_FOUND,
                        "projectId=" + projectId
                ));
        ExportPeriod period = resolvePeriod(startDate, endDate, false);
        String filename = buildProjectFilename(project, period);
        return export(projectId, period, status, fulfillmentMethod, filename);
    }

    @Transactional(readOnly = true)
    public AdminOrderExportResponseDto exportOrdersByDate(
            LocalDate startDate,
            LocalDate endDate,
            OrderStatus status,
            OrderFulfillmentMethod fulfillmentMethod
    ) {
        ExportPeriod period = resolvePeriod(startDate, endDate, true);
        String filename = "주문목록_" + period.fileDateRange() + ".csv";
        return export(null, period, status, fulfillmentMethod, filename);
    }

    private AdminOrderExportResponseDto export(
            Long projectId,
            ExportPeriod period,
            OrderStatus status,
            OrderFulfillmentMethod fulfillmentMethod,
            String filename
    ) {
        LocalDateTime startAt = period == null ? null : period.startAt();
        LocalDateTime endAtExclusive = period == null ? null : period.endAtExclusive();
        List<Order> orders = orderRepository.findAllForExport(
                projectId,
                startAt,
                endAtExclusive,
                status,
                fulfillmentMethod
        );

        if (orders.isEmpty()) {
            return new AdminOrderExportResponseDto(filename, createCsv(orders, Map.of(), Map.of(), Map.of()));
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, OrderBuyer> buyers = orderBuyerRepository.findAllByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(OrderBuyer::getOrderId, Function.identity()));
        Map<Long, OrderFulfillment> fulfillments = orderFulfillmentRepository.findAllByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(OrderFulfillment::getOrderId, Function.identity()));
        Map<Long, List<OrderItem>> items = orderItemRepository
                .findAllByOrderIdInOrderByOrderIdAndProjectItemId(orderIds)
                .stream()
                .collect(Collectors.groupingBy(
                        item -> item.getOrder().getId(),
                        Collectors.toList()
                ));

        return new AdminOrderExportResponseDto(filename, createCsv(orders, buyers, fulfillments, items));
    }

    private byte[] createCsv(
            List<Order> orders,
            Map<Long, OrderBuyer> buyers,
            Map<Long, OrderFulfillment> fulfillments,
            Map<Long, List<OrderItem>> itemsByOrderId
    ) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        appendRow(csv, CSV_HEADERS);

        for (Order order : orders) {
            Long orderId = order.getId();
            OrderBuyer buyer = getBuyer(orderId, buyers);
            OrderFulfillment fulfillment = getFulfillment(orderId, fulfillments);
            List<OrderItem> items = itemsByOrderId.getOrDefault(orderId, Collections.emptyList());

            String itemNames = items.stream()
                    .map(OrderItem::getItemNameSnapshot)
                    .collect(Collectors.joining(ITEM_SEPARATOR));
            String itemQuantities = items.stream()
                    .map(item -> Integer.toString(item.getQuantity()))
                    .collect(Collectors.joining(ITEM_SEPARATOR));

            appendRow(
                    csv,
                    order.getCreatedAt().format(ORDER_DATE_FORMATTER),
                    order.getOrderNo(),
                    order.getStatus().name(),
                    buyer.getName(),
                    buyer.getDepartmentOrMajor(),
                    buyer.getStudentNo(),
                    buyer.getPhone(),
                    Integer.toString(order.getFinalAmount()),
                    itemNames,
                    itemQuantities,
                    fulfillment.getMethod().name(),
                    buildAddress(fulfillment),
                    buyer.getRefundBank(),
                    buyer.getRefundAccount()
            );
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private OrderBuyer getBuyer(Long orderId, Map<Long, OrderBuyer> buyers) {
        OrderBuyer buyer = buyers.get(orderId);
        if (buyer == null) {
            throw new OrderException(OrderErrorType.BUYER_NOT_FOUND, "orderId=" + orderId);
        }
        return buyer;
    }

    private OrderFulfillment getFulfillment(Long orderId, Map<Long, OrderFulfillment> fulfillments) {
        OrderFulfillment fulfillment = fulfillments.get(orderId);
        if (fulfillment == null) {
            throw new OrderException(OrderErrorType.FULFILLMENT_NOT_FOUND, "orderId=" + orderId);
        }
        return fulfillment;
    }

    private String buildAddress(OrderFulfillment fulfillment) {
        if (fulfillment.getMethod() != OrderFulfillmentMethod.DELIVERY) {
            return "";
        }
        return List.of(
                        valueOrEmpty(fulfillment.getPostalCode()),
                        valueOrEmpty(fulfillment.getAddressLine1()),
                        valueOrEmpty(fulfillment.getAddressLine2())
                ).stream()
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(" "));
    }

    private void appendRow(StringBuilder csv, String... values) {
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                csv.append(',');
            }
            String value = protectFromSpreadsheetFormula(valueOrEmpty(values[index]));
            csv.append('"').append(value.replace("\"", "\"\"")).append('"');
        }
        csv.append("\r\n");
    }

    private String protectFromSpreadsheetFormula(String value) {
        String leadingStripped = value.stripLeading();
        if (leadingStripped.isEmpty()) {
            return value;
        }
        char firstCharacter = leadingStripped.charAt(0);
        if (firstCharacter == '=' || firstCharacter == '+' || firstCharacter == '-'
                || firstCharacter == '@' || firstCharacter == '\t' || firstCharacter == '\r') {
            return "'" + value;
        }
        return value;
    }

    private ExportPeriod resolvePeriod(LocalDate startDate, LocalDate endDate, boolean required) {
        if (startDate == null && endDate == null && !required) {
            return null;
        }
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new OrderException(OrderErrorType.INVALID_EXPORT_DATE_RANGE);
        }
        try {
            return new ExportPeriod(startDate, endDate.plusDays(1).atStartOfDay());
        } catch (DateTimeException exception) {
            throw new OrderException(OrderErrorType.INVALID_EXPORT_DATE_RANGE);
        }
    }

    private String buildProjectFilename(Project project, ExportPeriod period) {
        String projectTitle = sanitizeFilenamePart(project.getTitle());
        String dateSuffix = period == null ? "" : "_" + period.fileDateRange();
        return projectTitle + "_주문목록" + dateSuffix + ".csv";
    }

    private String sanitizeFilenamePart(String value) {
        String sanitized = value.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").strip();
        if (sanitized.isEmpty()) {
            return "프로젝트";
        }
        return sanitized.length() <= 60 ? sanitized : sanitized.substring(0, 60);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private record ExportPeriod(
            LocalDate startDate,
            LocalDateTime endAtExclusive
    ) {
        private LocalDateTime startAt() {
            return startDate.atStartOfDay();
        }

        private String fileDateRange() {
            LocalDate endDate = endAtExclusive.toLocalDate().minusDays(1);
            return startDate.format(FILE_DATE_FORMATTER) + "-" + endDate.format(FILE_DATE_FORMATTER);
        }
    }
}
