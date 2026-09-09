package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.request.AdminOrderCompletePageUpsertRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderCompletePageResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderCompletePage;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderCompletePageRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderCompletePageServiceTest {

    @Mock
    private OrderCompletePageRepository orderCompletePageRepository;

    private AdminOrderCompletePageService adminOrderCompletePageService;

    @BeforeEach
    void setUp() {
        adminOrderCompletePageService = new AdminOrderCompletePageService(orderCompletePageRepository);
    }

    @Test
    void upsertOrderCompletePage_기존행업데이트_성공() {
        // given
        OrderCompletePage existing = new OrderCompletePage("기존 제목", "기존 설명", "기존 결제 정보");
        when(orderCompletePageRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existing));

        AdminOrderCompletePageUpsertRequestDto request =
                new AdminOrderCompletePageUpsertRequestDto("새 제목", "새 설명", "새 결제 정보");

        // when
        AdminOrderCompletePageResponseDto response = adminOrderCompletePageService.upsertOrderCompletePage(request);

        // then
        assertThat(response.messageTitle()).isEqualTo("새 제목");
        assertThat(existing.getMessageTitle()).isEqualTo("새 제목");
        assertThat(existing.getMessageDescription()).isEqualTo("새 설명");
        assertThat(existing.getPaymentInformation()).isEqualTo("새 결제 정보");
        // 시드로 항상 존재하는 단일 행을 update만 하므로 신규 저장은 호출되지 않는다.
        verifyNoMoreInteractions(orderCompletePageRepository);
    }

    @Test
    void upsertOrderCompletePage_행없음_OrderException발생() {
        // given
        when(orderCompletePageRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        AdminOrderCompletePageUpsertRequestDto request =
                new AdminOrderCompletePageUpsertRequestDto("새 제목", "새 설명", "새 결제 정보");

        // when & then
        assertThatThrownBy(() -> adminOrderCompletePageService.upsertOrderCompletePage(request))
                .isInstanceOf(OrderException.class);
    }
}
