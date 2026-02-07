package com.example.cowmjucraft.domain.order.service;

import jakarta.mail.internet.InternetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ORDER_VIEW_SUBJECT = "[명지공방(明智工房)] 주문 조회 링크 안내";
    private static final String ORDER_STATUS_SUBJECT = "[명지공방(明智工房)] 주문 상태 변경 안내";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${mail.from}")
    private String from;

    @Value("${mail.from-name:CowMjuCraft}")
    private String fromName;

    public void sendOrderViewLink(String to, String buyerName, String orderNo, String viewUrl, LocalDateTime depositDeadline) {
        try {
            Context context = new Context();
            context.setVariable("orderNo", orderNo);
            context.setVariable("viewUrl", viewUrl);
            context.setVariable("depositDeadline", formatDateTime(depositDeadline));
            context.setVariable("buyerName", buyerName);
            String html = templateEngine.process("mail/order-email-template", context);

            sendHtml(to, ORDER_VIEW_SUBJECT, html, orderNo);
        } catch (Exception exception) {
            log.error("주문 조회 링크 메일 발송 실패: to={}, orderNo={}", to, orderNo, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "주문 메일 발송에 실패했습니다.");
        }
    }

    public void sendPaidConfirmed(String to, String buyerName, String orderNo, String viewUrl, LocalDateTime paidAt) {
        sendStatusMail(
                to,
                buyerName,
                orderNo,
                "결제확정",
                "결제 완료",
                null,
                "결제 시각",
                paidAt,
                viewUrl
        );
    }

    public void sendCanceled(String to, String buyerName, String orderNo, String viewUrl, String reason, LocalDateTime canceledAt) {
        sendStatusMail(
                to,
                buyerName,
                orderNo,
                "주문취소",
                "주문 취소",
                reason,
                "취소 시각",
                canceledAt,
                viewUrl
        );
    }

    public void sendRefundRequested(
            String to,
            String buyerName,
            String orderNo,
            String viewUrl,
            String reason,
            LocalDateTime refundRequestedAt
    ) {
        sendStatusMail(
                to,
                buyerName,
                orderNo,
                "환불요청접수",
                "환불 요청 접수",
                reason,
                "환불 요청 시각",
                refundRequestedAt,
                viewUrl
        );
    }

    public void sendRefunded(String to, String buyerName, String orderNo, String viewUrl, LocalDateTime refundedAt) {
        sendStatusMail(
                to,
                buyerName,
                orderNo,
                "환불완료",
                "환불 완료",
                null,
                "환불 완료 시각",
                refundedAt,
                viewUrl
        );
    }

    private void sendStatusMail(
            String to,
            String buyerName,
            String orderNo,
            String statusCode,
            String statusLabel,
            String reason,
            String eventTimeLabel,
            LocalDateTime eventTime,
            String viewUrl
    ) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<p>").append(escapeHtml(nullToEmpty(buyerName))).append("님, 주문 상태가 변경되었습니다.</p>")
                    .append("<p><strong>주문번호:</strong> ").append(escapeHtml(orderNo)).append("</p>")
                    .append("<p><strong>현재 상태:</strong> ").append(escapeHtml(statusLabel)).append(" (").append(escapeHtml(statusCode)).append(")</p>")
                    .append("<p><strong>").append(escapeHtml(eventTimeLabel)).append(":</strong> ")
                    .append(escapeHtml(formatDateTime(eventTime))).append("</p>");

            if (reason != null && !reason.isBlank()) {
                html.append("<p><strong>사유:</strong> ").append(escapeHtml(reason)).append("</p>");
            }

            html.append("<p><strong>주문 조회 링크:</strong> <a href=\"")
                    .append(escapeHtml(viewUrl))
                    .append("\">")
                    .append(escapeHtml(viewUrl))
                    .append("</a></p>");

            sendHtml(to, ORDER_STATUS_SUBJECT, html.toString(), orderNo);
        } catch (Exception exception) {
            log.error("주문 상태 메일 발송 실패: to={}, orderNo={}", to, orderNo, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "주문 메일 발송에 실패했습니다.");
        }
    }

    private void sendHtml(String to, String subject, String html, String orderNo) {
        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception exception) {
            log.error("메일 발송 실패: to={}, orderNo={}", to, orderNo, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "주문 메일 발송에 실패했습니다.");
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtml(String value) {
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
