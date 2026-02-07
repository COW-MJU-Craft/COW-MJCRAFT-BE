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

    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ORDER_VIEW_SUBJECT = "[명지공방(明智工房)] 주문 조회 링크 안내";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${mail.from}")
    private String from;

    @Value("${mail.from-name:CowMjuCraft}")
    private String fromName;

    public void sendOrderViewLink(String to,String buyerName, String orderNo, String viewUrl, LocalDateTime depositDeadline) {
        try {
            Context context = new Context();
            context.setVariable("orderNo", orderNo);
            context.setVariable("viewUrl", viewUrl);
            context.setVariable("depositDeadline", formatDeadline(depositDeadline));
            context.setVariable("buyerName", buyerName);
            String html = templateEngine.process("mail/order-email-template", context);

            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(ORDER_VIEW_SUBJECT);
            helper.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception exception) {
            log.error("주문 조회 링크 메일 발송 실패: to={}, orderNo={}", to, orderNo, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "주문 메일 발송에 실패했습니다.");
        }
    }

    private String formatDeadline(LocalDateTime deadline) {
        if (deadline == null) {
            return "-";
        }
        return deadline.format(DEADLINE_FORMATTER);
    }
}
