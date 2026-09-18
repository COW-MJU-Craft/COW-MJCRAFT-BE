package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.item.entity.ItemOptionGroup;
import com.example.cowmjucraft.domain.item.entity.ItemOptionValue;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ItemOptionValueRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.entity.MailOutboxEventType;
import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderItemOption;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemOptionRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminOrderPaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderItemOptionRepository orderItemOptionRepository;
    @Mock
    private ProjectItemRepository projectItemRepository;
    @Mock
    private ItemOptionValueRepository itemOptionValueRepository;
    @Mock
    private OrderBuyerRepository orderBuyerRepository;
    @Mock
    private OrderViewTokenService orderViewTokenService;
    @Mock
    private MailOutboxService mailOutboxService;

    private AdminOrderPaymentService adminOrderPaymentService;

    @BeforeEach
    void setUp() {
        adminOrderPaymentService = new AdminOrderPaymentService(
                orderRepository,
                orderItemRepository,
                orderItemOptionRepository,
                projectItemRepository,
                itemOptionValueRepository,
                orderBuyerRepository,
                orderViewTokenService,
                mailOutboxService
        );
    }

    @Test
    void confirmPaid_increasesGroupbuyFundedQuantity() {
        Order order = order();
        ProjectItem item = groupbuyItem(1L, 100, 40);
        OrderItem orderItem = new OrderItem(order, item, 3, 10_000, 30_000, "공동구매 상품");
        OrderBuyer buyer = buyer(order);

        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderIdOrderByProjectItemIdAsc(10L)).thenReturn(List.of(orderItem));
        when(projectItemRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(item));
        when(orderBuyerRepository.findById(10L)).thenReturn(Optional.of(buyer));
        when(orderViewTokenService.rotateToken(any(Order.class), any())).thenReturn("raw-token");
        when(orderViewTokenService.buildOrderViewUrl("raw-token")).thenReturn("https://example.com/orders/view?token=raw-token");

        adminOrderPaymentService.confirmPaid(10L);

        assertThat(item.getFundedQty()).isEqualTo(43);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(mailOutboxService).enqueueStatusMail(
                org.mockito.ArgumentMatchers.eq(MailOutboxEventType.PAID_CONFIRMED),
                org.mockito.ArgumentMatchers.eq(10L),
                any(), any(), any(), any(), org.mockito.ArgumentMatchers.isNull(), any());
    }

    @Test
    void confirmPaid_allowsGroupbuyFundedQuantityOverTargetQuantity() {
        Order order = order();
        ProjectItem item = groupbuyItem(1L, 100, 98);
        OrderItem orderItem = new OrderItem(order, item, 3, 10_000, 30_000, "공동구매 상품");
        OrderBuyer buyer = buyer(order);

        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderIdOrderByProjectItemIdAsc(10L)).thenReturn(List.of(orderItem));
        when(projectItemRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(item));
        when(orderBuyerRepository.findById(10L)).thenReturn(Optional.of(buyer));
        when(orderViewTokenService.rotateToken(any(Order.class), any())).thenReturn("raw-token");
        when(orderViewTokenService.buildOrderViewUrl("raw-token")).thenReturn("https://example.com/orders/view?token=raw-token");

        adminOrderPaymentService.confirmPaid(10L);

        assertThat(item.getFundedQty()).isEqualTo(101);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void confirmPaid_옵션상품이면_옵션값재고만차감하고상품재고는안건드린다() {
        // given
        Order order = order();
        ProjectItem item = normalItem(1L, 100);
        ItemOptionGroup group = optionGroup(item, 20L, "색상", 0);
        ItemOptionValue black = optionValue(group, 200L, "블랙", 10);
        OrderItem orderItem = new OrderItem(order, item, 2, 12_500, 25_000, "머그컵");
        ReflectionTestUtils.setField(orderItem, "id", 900L);
        OrderItemOption orderItemOption = new OrderItemOption(orderItem, black, "색상", "블랙", 500);
        OrderBuyer buyer = buyer(order);

        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderIdOrderByProjectItemIdAsc(10L)).thenReturn(List.of(orderItem));
        when(orderItemOptionRepository.findByOrderItemIdIn(List.of(900L))).thenReturn(List.of(orderItemOption));
        when(itemOptionValueRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(black));
        when(orderBuyerRepository.findById(10L)).thenReturn(Optional.of(buyer));
        when(orderViewTokenService.rotateToken(any(Order.class), any())).thenReturn("raw-token");
        when(orderViewTokenService.buildOrderViewUrl("raw-token")).thenReturn("https://example.com/orders/view?token=raw-token");

        // when
        adminOrderPaymentService.confirmPaid(10L);

        // then
        assertThat(black.getStockQty()).isEqualTo(8);
        assertThat(item.getStockQty()).isEqualTo(100);
        verify(projectItemRepository, org.mockito.Mockito.never()).findByIdForUpdate(any());
    }

    private ProjectItem normalItem(Long id, int stockQty) {
        ProjectItem item = new ProjectItem(
                project(1L),
                "머그컵",
                "summary",
                "description",
                12_000,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                "thumb.png",
                null,
                null,
                null,
                stockQty
        );
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    private ItemOptionGroup optionGroup(ProjectItem item, Long id, String name, int sortOrder) {
        ItemOptionGroup group = new ItemOptionGroup(item, name, true, sortOrder);
        ReflectionTestUtils.setField(group, "id", id);
        return group;
    }

    private ItemOptionValue optionValue(ItemOptionGroup group, Long id, String name, Integer stockQty) {
        ItemOptionValue value = new ItemOptionValue(group, name, 500, stockQty, 0);
        ReflectionTestUtils.setField(value, "id", id);
        return value;
    }

    private Order order() {
        Order order = new Order(
                "ORD-20260505120000-123456",
                testCustomer(),
                project(1L),
                1L,
                OrderStatus.PENDING_DEPOSIT,
                30_000,
                0,
                30_000,
                LocalDateTime.now().plusDays(1),
                "홍길동",
                true,
                LocalDateTime.now(),
                true,
                LocalDateTime.now(),
                true,
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(order, "id", 10L);
        return order;
    }

    private ProjectItem groupbuyItem(Long id, int targetQty, int fundedQty) {
        ProjectItem item = new ProjectItem(
                null,
                "공동구매 상품",
                "summary",
                "description",
                10_000,
                ItemSaleType.GROUPBUY,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                "thumb.png",
                null,
                targetQty,
                fundedQty,
                null
        );
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    private OrderBuyer buyer(Order order) {
        return new OrderBuyer(
                order,
                OrderBuyerType.STUDENT,
                "SEOUL",
                "홍길동",
                "컴퓨터공학과",
                "60123456",
                "010-1234-5678",
                "국민은행",
                "123456-78-901234",
                "instagram",
                "hong@example.com"
        );
    }

    private Customer testCustomer() {
        return new Customer("buyer@mju.ac.kr");
    }
}
