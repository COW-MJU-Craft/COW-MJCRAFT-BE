package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderAuth;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderViewToken;
import com.example.cowmjucraft.domain.order.repository.OrderAuthRepository;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderViewTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderQueryByTokenService {

    private final OrderViewTokenRepository orderViewTokenRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final OrderFulfillmentRepository orderFulfillmentRepository;
    private final OrderAuthRepository orderAuthRepository;

    @Transactional(readOnly = true)
    public String buildOrderViewHtml(String plainToken) {
        if (plainToken == null || plainToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token이 필요합니다.");
        }

        String tokenHash = hash(plainToken);
        OrderViewToken orderViewToken = orderViewTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유효하지 않은 조회 링크입니다."));

        if (orderViewToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "만료된 조회 링크입니다.");
        }

        Order order = orderViewToken.getOrder();
        List<OrderItem> items = orderItemRepository.findAllByOrderIdOrderByProjectItemIdAsc(order.getId());
        OrderBuyer buyer = orderBuyerRepository.findById(order.getId()).orElse(null);
        OrderFulfillment fulfillment = orderFulfillmentRepository.findById(order.getId()).orElse(null);
        OrderAuth auth = orderAuthRepository.findById(order.getId()).orElse(null);

        StringBuilder itemRows = new StringBuilder();
        for (OrderItem item : items) {
            itemRows.append("<li>")
                    .append(escape(item.getItemNameSnapshot()))
                    .append(" - ")
                    .append(item.getQuantity())
                    .append("개</li>");
        }

        return """
                <!doctype html>
                <html lang=\"ko\">
                <head>
                  <meta charset=\"UTF-8\" />
                  <title>주문 조회</title>
                </head>
                <body>
                  <h2>주문 조회</h2>
                  <p><strong>주문번호:</strong> %s</p>
                  <p><strong>상태:</strong> %s</p>
                  <p><strong>최종 금액:</strong> %d원</p>
                  <p><strong>입금 기한:</strong> %s</p>
                  <p><strong>조회 아이디:</strong> %s</p>
                  <hr/>
                  <h3>주문 상품</h3>
                  <ul>%s</ul>
                  <hr/>
                  <h3>주문자 정보</h3>
                  <p><strong>이름:</strong> %s</p>
                  <p><strong>이메일:</strong> %s</p>
                  <p><strong>전화번호:</strong> %s</p>
                  <hr/>
                  <h3>수령 정보</h3>
                  <p><strong>방법:</strong> %s</p>
                  <p><strong>수령인:</strong> %s</p>
                  <p><strong>연락처:</strong> %s</p>
                  <p><strong>주소:</strong> %s %s %s</p>
                </body>
                </html>
                """.formatted(
                escape(order.getOrderNo()),
                escape(order.getStatus().name()),
                order.getFinalAmount(),
                escape(String.valueOf(order.getDepositDeadline())),
                escape(auth == null ? "" : auth.getLookupId()),
                itemRows,
                escape(buyer == null ? "" : buyer.getName()),
                escape(buyer == null ? "" : buyer.getEmail()),
                escape(buyer == null ? "" : buyer.getPhone()),
                escape(fulfillment == null ? "" : fulfillment.getMethod().name()),
                escape(fulfillment == null ? "" : fulfillment.getReceiverName()),
                escape(fulfillment == null ? "" : fulfillment.getReceiverPhone()),
                escape(fulfillment == null ? "" : nullToEmpty(fulfillment.getPostalCode())),
                escape(fulfillment == null ? "" : nullToEmpty(fulfillment.getAddressLine1())),
                escape(fulfillment == null ? "" : nullToEmpty(fulfillment.getAddressLine2()))
        );
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 처리 중 오류가 발생했습니다.");
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
