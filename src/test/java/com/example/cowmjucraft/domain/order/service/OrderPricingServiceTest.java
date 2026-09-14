package com.example.cowmjucraft.domain.order.service;

import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.cowmjucraft.domain.item.entity.ItemOptionGroup;
import com.example.cowmjucraft.domain.item.entity.ItemOptionValue;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ItemOptionGroupRepository;
import com.example.cowmjucraft.domain.item.repository.ItemOptionValueRepository;
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
    private ItemOptionGroupRepository itemOptionGroupRepository;
    @Mock
    private ItemOptionValueRepository itemOptionValueRepository;
    @Mock
    private OrderPolicyRepository orderPolicyRepository;

    private OrderPricingService orderPricingService;

    @BeforeEach
    void setUp() {
        orderPricingService = new OrderPricingService(
                projectItemRepository,
                itemOptionGroupRepository,
                itemOptionValueRepository,
                orderPolicyRepository
        );
    }

    @Test
    void calculate_중복상품수량을합산하고택배비를추가한다() {
        // given
        ProjectItem item = item(1L, 12_000, 5);
        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item));
        given(orderPolicyRepository.findFirstByOrderByIdAsc()).willReturn(Optional.of(new OrderPolicy(3_500)));

        // when
        OrderPricingService.PriceQuote quote = orderPricingService.calculate(
                List.of(itemRequest(1L, 1), itemRequest(1L, 2)),
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
                List.of(itemRequest(1L, 1), itemRequest(2L, 2)),
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
                List.of(itemRequest(1L, 1)),
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
                List.of(itemRequest(1L, 1)),
                OrderFulfillmentMethod.PICKUP
        )).isInstanceOf(OrderException.class);
    }

    @Test
    void calculate_재고보다많은수량이면_OrderException발생() {
        // given
        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item(1L, 10_000, 1)));

        // when & then
        assertThatThrownBy(() -> orderPricingService.calculate(
                List.of(itemRequest(1L, 2)),
                OrderFulfillmentMethod.PICKUP
        )).isInstanceOf(OrderException.class);
    }

    @Test
    void calculate_선택사항옵션그룹만있는상품_옵션없이도상품재고로주문된다() {
        // given — "선물포장" 그룹은 required=false. 옵션을 하나도 안 골라도 상품 자체는 주문 가능해야 함
        // (AdminItemService 쪽에서 이 경우 상품 stockQty를 null로 강제하지 않으므로, 여기선 stockQty=5로 살아있음)
        ProjectItem item = item(1L, 12_000, 5);
        ItemOptionGroup giftWrapGroup = optionGroup(item, 100L, "선물포장", false, 0);

        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item));
        given(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(1L))).willReturn(List.of(giftWrapGroup));

        // when — 옵션을 하나도 선택하지 않음
        OrderPricingService.PriceQuote quote = orderPricingService.calculate(
                List.of(itemRequest(1L, 2)),
                OrderFulfillmentMethod.PICKUP
        );

        // then
        assertThat(quote.lines()).hasSize(1);
        assertThat(quote.totalAmount()).isEqualTo(24_000);
    }

    @Test
    void calculate_옵션추가금액이단가에반영된다() {
        // given
        ProjectItem item = item(1L, 12_000, 5);
        ItemOptionGroup colorGroup = optionGroup(item, 100L, "색상", true, 0);
        ItemOptionValue black = optionValue(colorGroup, 1000L, "블랙", 500, 10, 0);

        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item));
        given(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(1L))).willReturn(List.of(colorGroup));
        given(itemOptionValueRepository.findAllById(Set.of(1000L))).willReturn(List.of(black));

        // when
        OrderPricingService.PriceQuote quote = orderPricingService.calculate(
                List.of(itemRequest(1L, 2, 1000L)),
                OrderFulfillmentMethod.PICKUP
        );

        // then
        assertThat(quote.lines()).hasSize(1);
        assertThat(quote.lines().getFirst().unitPrice()).isEqualTo(12_500);
        assertThat(quote.totalAmount()).isEqualTo(25_000);
    }

    @Test
    void calculate_같은상품다른옵션조합_별도로집계된다() {
        // given
        ProjectItem item = item(1L, 12_000, 100);
        ItemOptionGroup colorGroup = optionGroup(item, 100L, "색상", true, 0);
        ItemOptionValue black = optionValue(colorGroup, 1000L, "블랙", 500, 10, 0);
        ItemOptionValue white = optionValue(colorGroup, 1001L, "화이트", 0, 10, 1);

        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item));
        given(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(1L))).willReturn(List.of(colorGroup));
        given(itemOptionValueRepository.findAllById(Set.of(1000L, 1001L))).willReturn(List.of(black, white));

        // when — 블랙 2개 + 화이트 1개는 같은 상품이지만 다른 옵션이라 합쳐지면 안 됨
        OrderPricingService.PriceQuote quote = orderPricingService.calculate(
                List.of(itemRequest(1L, 2, 1000L), itemRequest(1L, 1, 1001L)),
                OrderFulfillmentMethod.PICKUP
        );

        // then
        assertThat(quote.lines()).hasSize(2);
        assertThat(quote.totalAmount()).isEqualTo(2 * 12_500 + 12_000);
    }

    @Test
    void calculate_필수옵션누락_OrderException발생() {
        // given
        ProjectItem item = item(1L, 12_000, 5);
        ItemOptionGroup colorGroup = optionGroup(item, 100L, "색상", true, 0);

        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item));
        given(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(1L))).willReturn(List.of(colorGroup));

        // when & then — 옵션을 하나도 안 골랐는데 색상 그룹은 필수임
        assertThatThrownBy(() -> orderPricingService.calculate(
                List.of(itemRequest(1L, 1)),
                OrderFulfillmentMethod.PICKUP
        )).isInstanceOf(OrderException.class);
    }

    @Test
    void calculate_다른상품의옵션값선택_OrderException발생() {
        // given
        ProjectItem item = item(1L, 12_000, 5);
        ProjectItem otherItem = item(2L, 5_000, 5);
        ItemOptionGroup otherGroup = optionGroup(otherItem, 200L, "색상", true, 0);
        ItemOptionValue otherValue = optionValue(otherGroup, 2000L, "블랙", 0, 10, 0);

        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item));
        given(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(1L))).willReturn(List.of());
        given(itemOptionValueRepository.findAllById(Set.of(2000L))).willReturn(List.of(otherValue));

        // when & then — 2000L은 상품 1L이 아니라 2L의 옵션값
        assertThatThrownBy(() -> orderPricingService.calculate(
                List.of(itemRequest(1L, 1, 2000L)),
                OrderFulfillmentMethod.PICKUP
        )).isInstanceOf(OrderException.class);
    }

    @Test
    void calculate_같은그룹에서두개선택_OrderException발생() {
        // given
        ProjectItem item = item(1L, 12_000, 5);
        ItemOptionGroup colorGroup = optionGroup(item, 100L, "색상", true, 0);
        ItemOptionValue black = optionValue(colorGroup, 1000L, "블랙", 500, 10, 0);
        ItemOptionValue white = optionValue(colorGroup, 1001L, "화이트", 0, 10, 1);

        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item));
        given(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(1L))).willReturn(List.of(colorGroup));
        given(itemOptionValueRepository.findAllById(Set.of(1000L, 1001L))).willReturn(List.of(black, white));

        // when & then — 같은 "색상" 그룹에서 블랙과 화이트를 동시에 선택
        assertThatThrownBy(() -> orderPricingService.calculate(
                List.of(itemRequest(1L, 1, 1000L, 1001L)),
                OrderFulfillmentMethod.PICKUP
        )).isInstanceOf(OrderException.class);
    }

    @Test
    void calculate_옵션재고부족_OrderException발생() {
        // given
        ProjectItem item = item(1L, 12_000, 100);
        ItemOptionGroup colorGroup = optionGroup(item, 100L, "색상", true, 0);
        ItemOptionValue black = optionValue(colorGroup, 1000L, "블랙", 500, 1, 0);

        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item));
        given(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(1L))).willReturn(List.of(colorGroup));
        given(itemOptionValueRepository.findAllById(Set.of(1000L))).willReturn(List.of(black));

        // when & then — 블랙 재고는 1개뿐인데 2개 주문
        assertThatThrownBy(() -> orderPricingService.calculate(
                List.of(itemRequest(1L, 2, 1000L)),
                OrderFulfillmentMethod.PICKUP
        )).isInstanceOf(OrderException.class);
    }

    @Test
    void calculate_옵션재고null이면무제한판매() {
        // given
        ProjectItem item = item(1L, 12_000, 100);
        ItemOptionGroup colorGroup = optionGroup(item, 100L, "색상", true, 0);
        ItemOptionValue black = optionValue(colorGroup, 1000L, "블랙", 500, null, 0);

        given(projectItemRepository.findAllById(Set.of(1L))).willReturn(List.of(item));
        given(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(1L))).willReturn(List.of(colorGroup));
        given(itemOptionValueRepository.findAllById(Set.of(1000L))).willReturn(List.of(black));

        // when
        OrderPricingService.PriceQuote quote = orderPricingService.calculate(
                List.of(itemRequest(1L, 999, 1000L)),
                OrderFulfillmentMethod.PICKUP
        );

        // then
        assertThat(quote.lines()).hasSize(1);
    }

    private OrderCreateItemRequestDto itemRequest(Long projectItemId, int quantity) {
        return new OrderCreateItemRequestDto(projectItemId, quantity, null);
    }

    private OrderCreateItemRequestDto itemRequest(Long projectItemId, int quantity, Long... optionValueIds) {
        return new OrderCreateItemRequestDto(projectItemId, quantity, List.of(optionValueIds));
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

    private ItemOptionGroup optionGroup(ProjectItem item, Long id, String name, boolean required, int sortOrder) {
        ItemOptionGroup group = new ItemOptionGroup(item, name, required, sortOrder);
        ReflectionTestUtils.setField(group, "id", id);
        return group;
    }

    private ItemOptionValue optionValue(
            ItemOptionGroup group,
            Long id,
            String name,
            int additionalPrice,
            Integer stockQty,
            int sortOrder
    ) {
        ItemOptionValue value = new ItemOptionValue(group, name, additionalPrice, stockQty, sortOrder);
        ReflectionTestUtils.setField(value, "id", id);
        return value;
    }
}
