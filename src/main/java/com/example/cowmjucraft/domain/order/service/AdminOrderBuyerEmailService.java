package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.customer.service.CustomerAccountService;
import com.example.cowmjucraft.domain.customer.service.EmailNormalizer;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문자 이메일 오타를 관리자가 정정한다.
 *
 * <p>이메일이 유일한 연락 수단이자 고객 식별 키라서, 오타가 나면 그 주문은
 * 완료 메일도 조회도 전부 막힌 미아가 된다. 사용자가 스스로 고칠 방법이 없으므로
 * 이 API가 실질적인 안전망이다.
 *
 * <p>정정하면 ① 주문 스냅샷의 이메일 ② 주문이 속한 고객 ③ 조회 링크 메일
 * 세 가지를 함께 맞춘다.
 */
@Service
@RequiredArgsConstructor
public class AdminOrderBuyerEmailService {

    private final OrderRepository orderRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final CustomerAccountService customerAccountService;
    private final OrderViewTokenService orderViewTokenService;
    private final MailOutboxService mailOutboxService;

    @Transactional
    public void correctBuyerEmail(Long orderId, String rawEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_NOT_FOUND, "orderId=" + orderId));
        OrderBuyer buyer = orderBuyerRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorType.BUYER_NOT_FOUND));

        String email = EmailNormalizer.normalize(rawEmail);
        LocalDateTime now = LocalDateTime.now();

        buyer.correctEmail(email);

        Customer customer = customerAccountService.upsertForOrder(email, now);
        order.reassignCustomer(customer);

        // 잘못된 주소로 나간 링크는 폐기하고 새 링크를 정정된 주소로 다시 보낸다.
        String rawViewToken = orderViewTokenService.rotateToken(order, now);
        mailOutboxService.enqueueOrderViewLink(
                order.getId(),
                email,
                buyer.getName(),
                order.getOrderNo(),
                orderViewTokenService.buildOrderViewUrl(rawViewToken),
                order.getDepositDeadline()
        );
    }
}
