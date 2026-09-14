package com.example.cowmjucraft.domain.order.service;

import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateItemRequestDto;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderPolicy;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderPolicyRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderPricingServiceTest {

    @Mock
    private ProjectItemRepository projectItemRepository;
    @Mock
    private OrderPolicyRepository orderPolicyRepository;

    private OrderPricingService orderPricingService;

    @BeforeEach
    void setUp() {
        orderPricingService = new OrderPricingService(projectItemRepository, orderPolicyRepository);
    }

    @Test
    void calculate_중복상품수량을합산하고택배비를추가한다() {
        // given
        ProjectItem item = item(1L, 12_000, 5);
        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item));
        given(orderPolicyRepository.findFirstByOrderByIdAsc()).willReturn(Optional.of(new OrderPolicy(3_500)));

        // when
        OrderPricingService.PriceQuote quote = orderPricingService.calculate(
                List.of(new OrderCreateItemRequestDto(1L, 1), new OrderCreateItemRequestDto(1L, 2)),
                OrderFulfillmentMethod.DELIVERY
        );

        // then
        assertThat(quote.lines()).hasSize(1);
        assertThat(quote.lines().getFirst().quantity()).isEqualTo(3);
        assertThat(quote.totalAmount()).isEqualTo(36_000);
        assertThat(quote.shippingFee()).isEqualTo(3_500);
        assertThat(quote.finalAmount()).isEqualTo(39_500);
    }

    @Test
    void calculate_상품이여러개여도조회는한번만한다() {
        // given
        ProjectItem first = item(1L, 10_000, 5);
        ProjectItem second = item(2L, 4_000, 5);
        given(projectItemRepository.findAllById(Set.of(1L, 2L))).willReturn(List.of(first, second));

        // when
        OrderPricingService.PriceQuote quote = orderPricingService.calculate(
                List.of(new OrderCreateItemRequestDto(1L, 1), new OrderCreateItemRequestDto(2L, 2)),
                OrderFulfillmentMethod.PICKUP
        );

        // then
        assertThat(quote.lines()).extracting(line -> line.projectItem().getId()).containsExactly(1L, 2L);
        assertThat(quote.totalAmount()).isEqualTo(18_000);
        verify(projectItemRepository).findAllById(Set.of(1L, 2L));
    }

    @Test
    void calculate_현장수령은배송정책을조회하지않는다() {
        // given
        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item(1L, 10_000, 1)));

        // when
        OrderPricingService.PriceQuote quote = orderPricingService.calculate(
                List.of(new OrderCreateItemRequestDto(1L, 1)),
                OrderFulfillmentMethod.PICKUP
        );

        // then
        assertThat(quote.shippingFee()).isZero();
        assertThat(quote.finalAmount()).isEqualTo(10_000);
        verify(orderPolicyRepository, never()).findFirstByOrderByIdAsc();
    }

    @Test
    void calculate_존재하지않는상품이면_OrderException발생() {
        // given
        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> orderPricingService.calculate(
                List.of(new OrderCreateItemRequestDto(1L, 1)),
                OrderFulfillmentMethod.PICKUP
        )).isInstanceOf(OrderException.class);
    }

    @Test
    void calculate_재고보다많은수량이면_OrderException발생() {
        // given
        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item(1L, 10_000, 1)));

        // when & then
        assertThatThrownBy(() -> orderPricingService.calculate(
                List.of(new OrderCreateItemRequestDto(1L, 2)),
                OrderFulfillmentMethod.PICKUP
        )).isInstanceOf(OrderException.class);
    }

    private ProjectItem item(Long id, int price, int stockQty) {
        ProjectItem item = new ProjectItem(
                project(10L),
                "일반 상품",
                "summary",
                "description",
                price,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                null,
                null,
                null,
                null,
                stockQty
        );
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }
}
