package com.example.cowmjucraft.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderExportResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectCategory;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminOrderExportServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderBuyerRepository orderBuyerRepository;
    @Mock
    private OrderFulfillmentRepository orderFulfillmentRepository;

    private AdminOrderExportService adminOrderExportService;

    @BeforeEach
    void setUp() {
        adminOrderExportService = new AdminOrderExportService(
                projectRepository,
                orderRepository,
                orderItemRepository,
                orderBuyerRepository,
                orderFulfillmentRepository
        );
    }

    @Test
    void exportProjectOrders_주문상세정보_CSV파일반환() {
        // given
        Project project = project(1L, "가을 프로젝트");
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 3, 14, 25, 30);
        Order order = order(10L, project, createdAt);
        OrderBuyer buyer = buyer(order, "=홍길동");
        OrderFulfillment fulfillment = fulfillment(order, OrderFulfillmentMethod.DELIVERY);
        List<OrderItem> items = List.of(
                orderItem(100L, order, project, "티셔츠, 검정", 2),
                orderItem(200L, order, project, "스티커 \"A\"", 1)
        );
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 5);

        given(projectRepository.findById(1L)).willReturn(Optional.of(project));
        given(orderRepository.findAllForExport(
                1L,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                OrderStatus.PAID,
                OrderFulfillmentMethod.DELIVERY
        )).willReturn(List.of(order));
        given(orderBuyerRepository.findAllByOrderIdIn(List.of(10L))).willReturn(List.of(buyer));
        given(orderFulfillmentRepository.findAllByOrderIdIn(List.of(10L))).willReturn(List.of(fulfillment));
        given(orderItemRepository.findAllByOrderIdInOrderByOrderIdAndProjectItemId(List.of(10L)))
                .willReturn(items);

        // when
        AdminOrderExportResponseDto response = adminOrderExportService.exportProjectOrders(
                1L,
                startDate,
                endDate,
                OrderStatus.PAID,
                OrderFulfillmentMethod.DELIVERY
        );

        // then
        String csv = new String(response.content(), StandardCharsets.UTF_8);
        assertThat(response.filename()).isEqualTo("가을 프로젝트_주문목록_20260901-20260905.csv");
        assertThat(csv).startsWith("\uFEFF\"주문일자\"");
        assertThat(csv).contains("\"2026-09-03 14:25:30\"");
        assertThat(csv).contains("\"ORD-10\"");
        assertThat(csv).contains("\"'=홍길동\"");
        assertThat(csv).contains("\"티셔츠, 검정 | 스티커 \"\"A\"\"\"");
        assertThat(csv).contains("\"2 | 1\"");
        assertThat(csv).contains("\"04524 서울시 중구 101호\"");
        assertThat(csv).doesNotContain("상품 옵션");
    }

    @Test
    void exportOrdersByDate_주문없음_헤더만포함한CSV반환() {
        // given
        LocalDate date = LocalDate.of(2026, 9, 5);
        given(orderRepository.findAllForExport(
                null,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(),
                null,
                null
        )).willReturn(List.of());

        // when
        AdminOrderExportResponseDto response = adminOrderExportService.exportOrdersByDate(
                date,
                date,
                null,
                null
        );

        // then
        String csv = new String(response.content(), StandardCharsets.UTF_8);
        assertThat(response.filename()).isEqualTo("주문목록_20260905-20260905.csv");
        assertThat(csv.lines()).hasSize(1);
        assertThat(csv).contains("\"환불계좌\"");
        verifyNoInteractions(orderBuyerRepository, orderFulfillmentRepository, orderItemRepository);
    }

    @Test
    void exportProjectOrders_종료일누락_OrderException발생() {
        // given
        Project project = project(1L, "프로젝트");
        given(projectRepository.findById(1L)).willReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> adminOrderExportService.exportProjectOrders(
                1L,
                LocalDate.of(2026, 9, 1),
                null,
                null,
                null
        )).isInstanceOf(OrderException.class);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void exportOrdersByDate_시작일이종료일보다늦음_OrderException발생() {
        // when & then
        assertThatThrownBy(() -> adminOrderExportService.exportOrdersByDate(
                LocalDate.of(2026, 9, 6),
                LocalDate.of(2026, 9, 5),
                null,
                null
        )).isInstanceOf(OrderException.class);
        verifyNoInteractions(orderRepository);
    }

    private Project project(Long id, String title) {
        Project project = new Project(
                title,
                "요약",
                "설명",
                "thumbnail.png",
                List.of(),
                LocalDate.of(2026, 12, 31),
                ProjectStatus.OPEN,
                ProjectCategory.GOODS
        );
        ReflectionTestUtils.setField(project, "id", id);
        return project;
    }

    private Order order(Long id, Project project, LocalDateTime createdAt) {
        Order order = new Order(
                "ORD-" + id,
                project,
                1L,
                OrderStatus.PAID,
                20000,
                3000,
                23000,
                createdAt.plusDays(1),
                "홍길동",
                true,
                createdAt,
                true,
                createdAt,
                true,
                createdAt
        );
        ReflectionTestUtils.setField(order, "id", id);
        ReflectionTestUtils.setField(order, "createdAt", createdAt);
        return order;
    }

    private OrderBuyer buyer(Order order, String name) {
        OrderBuyer buyer = new OrderBuyer(
                order,
                OrderBuyerType.STUDENT,
                "인문캠퍼스",
                name,
                "컴퓨터공학과",
                "60123456",
                "010-1234-5678",
                "국민은행",
                "123456-78-901234",
                null,
                "buyer@example.com"
        );
        ReflectionTestUtils.setField(buyer, "orderId", order.getId());
        return buyer;
    }

    private OrderFulfillment fulfillment(Order order, OrderFulfillmentMethod method) {
        OrderFulfillment fulfillment = new OrderFulfillment(
                order,
                method,
                "홍길동",
                "010-1234-5678",
                true,
                "04524",
                "서울시 중구",
                "101호",
                null
        );
        ReflectionTestUtils.setField(fulfillment, "orderId", order.getId());
        return fulfillment;
    }

    private OrderItem orderItem(Long id, Order order, Project project, String name, int quantity) {
        ProjectItem item = new ProjectItem(
                project,
                name,
                null,
                "설명",
                10000,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                "thumbnail.png",
                null,
                null,
                null,
                10
        );
        ReflectionTestUtils.setField(item, "id", id);
        return new OrderItem(order, item, quantity, 10000, 10000 * quantity, name);
    }
}
