package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderTrackingInformationResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.project.exception.ProjectErrorType;
import com.example.cowmjucraft.domain.project.exception.ProjectException;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderTrackingInformationService {

    private final ProjectRepository projectRepository;
    private final OrderRepository orderRepository;
    private final OrderFulfillmentRepository orderFulfillmentRepository;

    @Transactional
    public AdminOrderTrackingInformationResponseDto updateTrackingInformation(
            Long projectId,
            Long orderId,
            String trackingInformation
    ) {
        validateProjectExists(projectId);
        validateOrderBelongsToProject(projectId, orderId);

        OrderFulfillment fulfillment = orderFulfillmentRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorType.FULFILLMENT_NOT_FOUND, "orderId=" + orderId));
        String normalizedTrackingInformation = normalizeOptionalText(trackingInformation);
        fulfillment.updateTrackingInformation(normalizedTrackingInformation);

        return new AdminOrderTrackingInformationResponseDto(orderId, normalizedTrackingInformation);
    }

    private void validateOrderBelongsToProject(Long projectId, Long orderId) {
        if (!orderRepository.existsByIdAndRepresentativeProjectId(orderId, projectId)) {
            throw new OrderException(
                    OrderErrorType.ORDER_NOT_FOUND,
                    "projectId=" + projectId + ", orderId=" + orderId
            );
        }
    }

    private void validateProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectException(ProjectErrorType.PROJECT_NOT_FOUND, "projectId=" + projectId);
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
