package com.example.cowmjucraft.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailOutboxDeliveryService {

    private final EmailService emailService;

    public void deliver(MailOutboxMessage message) {
        switch (message.eventType()) {
            case ORDER_VIEW_LINK -> emailService.sendOrderViewLink(
                    message.recipient(), message.buyerName(), message.orderNo(), message.viewUrl(), message.depositDeadline());
            case PAID_CONFIRMED -> emailService.sendPaidConfirmed(
                    message.recipient(), message.buyerName(), message.orderNo(), message.viewUrl(), message.eventAt());
            case CANCELED -> emailService.sendCanceled(
                    message.recipient(), message.buyerName(), message.orderNo(), message.viewUrl(), message.reason(), message.eventAt());
            case REFUND_REQUESTED -> emailService.sendRefundRequested(
                    message.recipient(), message.buyerName(), message.orderNo(), message.viewUrl(), message.reason(), message.eventAt());
            case REFUNDED -> emailService.sendRefunded(
                    message.recipient(), message.buyerName(), message.orderNo(), message.viewUrl(), message.eventAt());
        }
    }
}
