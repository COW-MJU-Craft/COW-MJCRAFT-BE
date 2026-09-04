package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateBuyerRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateFulfillmentRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateItemRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderCreateRequestDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyerType;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderPolicy;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderAuthRepository;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderFulfillmentRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderPolicyRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.global.security.PasswordPolicy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;

@ExtendWith(MockitoExtension.class)
class OrderCreateServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderBuyerRepository orderBuyerRepository;
    @Mock
    private OrderFulfillmentRepository orderFulfillmentRepository;
    @Mock
    private OrderAuthRepository orderAuthRepository;
    @Mock
    private ProjectItemRepository projectItemRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OrderViewTokenService orderViewTokenService;
    @Mock
    private MailOutboxService mailOutboxService;
    @Mock
    private OrderPolicyRepository orderPolicyRepository;
    @Mock
    private ProjectRepository projectRepository;

    private OrderCreateService orderCreateService;
    private Project representativeProject;

    @BeforeEach
    void setUp() {
        representativeProject = project(10L);
        org.mockito.Mockito.lenient().when(projectRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(representativeProject));
        orderCreateService = new OrderCreateService(
                orderRepository,
                orderItemRepository,
                orderBuyerRepository,
                orderFulfillmentRepository,
                orderAuthRepository,
                projectItemRepository,
                passwordEncoder,
                orderViewTokenService,
                mailOutboxService,
                new PasswordPolicy(),
                orderPolicyRepository,
                projectRepository
        );
    }

    @Test
    void createOrder_allowsGroupbuyItemWithinRemainingQuantity() {
        ProjectItem item = groupbuyItem(1L, 100, 40);
        when(orderAuthRepository.existsByLookupId("guest-mju-001")).thenReturn(false);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 10L);
            return order;
        });
        when(passwordEncoder.encode("Pa$$w0rd!")).thenReturn("encoded-password");
        when(orderViewTokenService.issueNewToken(any(Order.class), any())).thenReturn("raw-token");
        when(orderViewTokenService.buildOrderViewUrl("raw-token")).thenReturn("https://example.com/orders/view?token=raw-token");

        var response = orderCreateService.createOrder(request(60));

        assertThat(response.representativeProjectId()).isEqualTo(10L);
        assertThat(response.projectOrderNo()).isEqualTo(1L);
        assertThat(response.orderNo()).matches("P10-1-\\d{8}-\\d{6}");
        verify(orderItemRepository).saveAll(any());
        verify(mailOutboxService).enqueueOrderViewLink(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void createOrder_allowsGroupbuyItemOverRemainingQuantity() {
        ProjectItem item = groupbuyItem(1L, 100, 40);
        when(orderAuthRepository.existsByLookupId("guest-mju-001")).thenReturn(false);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 10L);
            return order;
        });
        when(passwordEncoder.encode("Pa$$w0rd!")).thenReturn("encoded-password");
        when(orderViewTokenService.issueNewToken(any(Order.class), any())).thenReturn("raw-token");
        when(orderViewTokenService.buildOrderViewUrl("raw-token")).thenReturn("https://example.com/orders/view?token=raw-token");

        orderCreateService.createOrder(request(61));

        verify(orderItemRepository).saveAll(any());
    }

    @Test
    void createOrder_비밀번호가정책미달_OrderException발생() {
        // given
        OrderCreateRequestDto weakPasswordRequest = request(1, "1234");

        // when & then
        assertThatThrownBy(() -> orderCreateService.createOrder(weakPasswordRequest))
                .isInstanceOf(OrderException.class);
    }

    @Test
    void createOrder_택배선택시_배송비자동추가() {
        // given
        ProjectItem item = groupbuyItem(1L, 100, 40);
        when(orderAuthRepository.existsByLookupId("guest-mju-001")).thenReturn(false);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderPolicyRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(new OrderPolicy(3500)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 10L);
            return order;
        });
        when(passwordEncoder.encode("Pa$$w0rd!")).thenReturn("encoded-password");
        when(orderViewTokenService.issueNewToken(any(Order.class), any())).thenReturn("raw-token");
        when(orderViewTokenService.buildOrderViewUrl("raw-token")).thenReturn("https://example.com/orders/view?token=raw-token");

        // when
        var response = orderCreateService.createOrder(deliveryRequest(1));

        // then
        assertThat(response.shippingFee()).isEqualTo(3500);
    }

    @Test
    void createOrder_현장수령시_배송비0원_정책조회안함() {
        // given
        ProjectItem item = groupbuyItem(1L, 100, 40);
        when(orderAuthRepository.existsByLookupId("guest-mju-001")).thenReturn(false);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 10L);
            return order;
        });
        when(passwordEncoder.encode("Pa$$w0rd!")).thenReturn("encoded-password");
        when(orderViewTokenService.issueNewToken(any(Order.class), any())).thenReturn("raw-token");
        when(orderViewTokenService.buildOrderViewUrl("raw-token")).thenReturn("https://example.com/orders/view?token=raw-token");

        // when
        var response = orderCreateService.createOrder(request(1));

        // then
        assertThat(response.shippingFee()).isEqualTo(0);
        verify(orderPolicyRepository, never()).findFirstByOrderByIdAsc();
    }

    @Test
    void createOrder_여러프로젝트상품_첫상품프로젝트로주문번호발급() {
        // given
        Project firstProject = project(20L);
        Project secondProject = project(30L);
        ProjectItem firstItem = groupbuyItem(2L, firstProject, 100, 0);
        ProjectItem secondItem = groupbuyItem(3L, secondProject, 100, 0);
        OrderCreateRequestDto baseRequest = request(1);
        OrderCreateRequestDto multiProjectRequest = new OrderCreateRequestDto(
                baseRequest.lookupId(),
                baseRequest.password(),
                baseRequest.depositorName(),
                baseRequest.privacyAgreed(),
                baseRequest.refundAgreed(),
                baseRequest.cancelRiskAgreed(),
                List.of(
                        new OrderCreateItemRequestDto(2L, 1),
                        new OrderCreateItemRequestDto(3L, 1)
                ),
                baseRequest.buyer(),
                baseRequest.fulfillment()
        );
        when(orderAuthRepository.existsByLookupId("guest-mju-001")).thenReturn(false);
        when(projectItemRepository.findById(2L)).thenReturn(Optional.of(firstItem));
        when(projectItemRepository.findById(3L)).thenReturn(Optional.of(secondItem));
        when(projectRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(firstProject));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 10L);
            return order;
        });
        when(passwordEncoder.encode("Pa$$w0rd!")).thenReturn("encoded-password");
        when(orderViewTokenService.issueNewToken(any(Order.class), any())).thenReturn("raw-token");
        when(orderViewTokenService.buildOrderViewUrl("raw-token"))
                .thenReturn("https://example.com/orders/view?token=raw-token");

        // when
        var response = orderCreateService.createOrder(multiProjectRequest);

        // then
        assertThat(response.representativeProjectId()).isEqualTo(20L);
        assertThat(response.projectOrderNo()).isEqualTo(1L);
        assertThat(response.orderNo()).matches("P20-1-\\d{8}-\\d{6}");
        assertThat(secondProject.getLastOrderNo()).isZero();
    }

    private ProjectItem groupbuyItem(Long id, int targetQty, int fundedQty) {
        return groupbuyItem(id, representativeProject, targetQty, fundedQty);
    }

    private ProjectItem groupbuyItem(Long id, Project project, int targetQty, int fundedQty) {
        ProjectItem item = new ProjectItem(
                project,
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

    private OrderCreateRequestDto request(int quantity) {
        return request(quantity, "Pa$$w0rd!");
    }

    private OrderCreateRequestDto request(int quantity, String password) {
        return new OrderCreateRequestDto(
                "guest-mju-001",
                password,
                "홍길동",
                true,
                true,
                true,
                List.of(new OrderCreateItemRequestDto(1L, quantity)),
                new OrderCreateBuyerRequestDto(
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
                ),
                new OrderCreateFulfillmentRequestDto(
                        OrderFulfillmentMethod.PICKUP,
                        "홍길동",
                        "010-1234-5678",
                        true,
                        null,
                        null,
                        null,
                        null
                )
        );
    }

    private OrderCreateRequestDto deliveryRequest(int quantity) {
        return new OrderCreateRequestDto(
                "guest-mju-001",
                "Pa$$w0rd!",
                "홍길동",
                true,
                true,
                true,
                List.of(new OrderCreateItemRequestDto(1L, quantity)),
                new OrderCreateBuyerRequestDto(
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
                ),
                new OrderCreateFulfillmentRequestDto(
                        OrderFulfillmentMethod.DELIVERY,
                        "홍길동",
                        "010-1234-5678",
                        true,
                        "04524",
                        "서울시 중구 세종대로 110",
                        "101동 1001호",
                        null
                )
        );
    }
}
