package com.example.cowmjucraft.domain.customer.service;

import com.example.cowmjucraft.global.config.AppProperties;
import jakarta.mail.internet.InternetAddress;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * 인증 코드 메일을 <b>동기</b> 발송한다.
 *
 * <p>{@code MailOutbox}를 쓰지 않는 이유가 둘 있다. 첫째, 그 스키마는 주문 전용이라
 * ({@code aggregate_id}·{@code order_no}·{@code view_url}이 NOT NULL) 코드 메일에는
 * 억지로 더미 값을 채워야 하고, {@code (event_type, aggregate_id, aggregate_version)}
 * 유니크 제약이 같은 이메일 재발송을 막는다. 둘째, 코드는 10분짜리라 워커 폴링
 * 지연을 얹을 이유가 없다.
 *
 * <p>SMTP 타임아웃은 {@code application.yml}에서 연결 5초·읽기 10초로 묶여 있어
 * 요청이 무한정 붙잡히지 않는다. 발송 실패는 호출부가 삼킨다 — 응답은 성공 여부와
 * 무관하게 동일해야 하기 때문이다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerCodeMailSender {

    private static final String SUBJECT = "[명지공방(明智工房)] 이메일 인증 코드 안내";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @Value("${mail.from}")
    private String from;

    @Value("${mail.from-name:CowMjuCraft}")
    private String fromName;

    /**
     * @throws RuntimeException 발송에 실패하면 던진다. 호출부가 잡아서 삼켜야 한다.
     */
    public void send(String to, String code) {
        try {
            Context context = new Context();
            context.setVariable("code", code);
            context.setVariable("ttlMinutes", appProperties.getCustomerCodeTtlMinutes());
            String html = templateEngine.process("mail/customer-code-email-template", context);

            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(SUBJECT);
            helper.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception exception) {
            // 코드 자체는 로그에 남기지 않는다.
            throw new IllegalStateException("인증 코드 메일 발송 실패: to=" + to, exception);
        }
    }
}
