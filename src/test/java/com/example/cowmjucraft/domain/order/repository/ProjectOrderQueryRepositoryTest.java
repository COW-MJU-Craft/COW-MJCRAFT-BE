package com.example.cowmjucraft.domain.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectCategory;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class ProjectOrderQueryRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void findAllByRepresentativeProjectIdAndStatusOrderByCreatedAtDesc_대표프로젝트주문만조회() {
        // given
        TestData data = persistTestData();

        // when
        List<Order> all = orderRepository.findAllByRepresentativeProjectIdAndStatusOrderByCreatedAtDesc(
                data.firstProject().getId(),
                null
        );
        List<Order> paid = orderRepository.findAllByRepresentativeProjectIdAndStatusOrderByCreatedAtDesc(
                data.firstProject().getId(),
                OrderStatus.PAID
        );
        List<Order> nonRepresentativeProjectOrders = orderRepository
                .findAllByRepresentativeProjectIdAndStatusOrderByCreatedAtDesc(data.secondProject().getId(), null);

        // then
        assertThat(all).extracting(Order::getId)
                .containsExactlyInAnyOrder(data.paidOrder().getId(), data.pendingOrder().getId());
        assertThat(paid).extracting(Order::getId).containsExactly(data.paidOrder().getId());
        assertThat(nonRepresentativeProjectOrders).isEmpty();
    }

    @Test
    void calculateProjectOrderStatistics_혼합프로젝트주문_해당프로젝트상품금액만집계() {
        // given
        TestData data = persistTestData();

        // when
        ProjectOrderStatisticsProjection statistics = orderItemRepository.calculateProjectOrderStatistics(
                data.firstProject().getId(),
                EnumSet.of(
                        OrderStatus.PAID,
                        OrderStatus.IN_PRODUCTION,
                        OrderStatus.READY_TO_SHIP,
                        OrderStatus.DELIVERED,
                        OrderStatus.REFUND_REQUESTED
                )
        );

        // then
        assertThat(statistics.getOrderCount()).isEqualTo(1L);
        assertThat(statistics.getTotalOrderAmount()).isEqualTo(15000L);
    }

    @Test
    void findAllByStatusOrderedForPublic_OPEN필터_OPEN프로젝트만조회() {
        // given
        TestData data = persistTestData();

        // when
        List<Project> projects = projectRepository.findAllByStatusOrderedForPublic(ProjectStatus.OPEN);

        // then
        assertThat(projects).extracting(Project::getId).containsExactly(data.firstProject().getId());
    }

    private TestData persistTestData() {
        Project firstProject = project("첫 번째 프로젝트", ProjectStatus.OPEN);
        Project secondProject = project("두 번째 프로젝트", ProjectStatus.CLOSED);
        ProjectItem firstItem = item(firstProject, "첫 번째 상품", 10000);
        ProjectItem firstAdditionalItem = item(firstProject, "첫 번째 프로젝트 추가 상품", 5000);
        ProjectItem secondItem = item(secondProject, "두 번째 상품", 20000);

        Order paidOrder = order("ORD-PAID", firstProject, 1L, OrderStatus.PAID, 35000);
        Order pendingOrder = order("ORD-PENDING", firstProject, 2L, OrderStatus.PENDING_DEPOSIT, 5000);

        entityManager.persist(new OrderItem(paidOrder, firstItem, 1, 10000, 10000, firstItem.getName()));
        entityManager.persist(new OrderItem(
                paidOrder,
                firstAdditionalItem,
                1,
                5000,
                5000,
                firstAdditionalItem.getName()
        ));
        entityManager.persist(new OrderItem(paidOrder, secondItem, 1, 20000, 20000, secondItem.getName()));
        entityManager.persist(new OrderItem(pendingOrder, firstItem, 1, 5000, 5000, firstItem.getName()));
        entityManager.flush();
        entityManager.clear();

        return new TestData(firstProject, secondProject, paidOrder, pendingOrder);
    }

    private Project project(String title, ProjectStatus status) {
        Project project = new Project(
                title,
                "요약",
                "설명",
                "thumbnail.png",
                List.of(),
                LocalDate.now().plusDays(7),
                status,
                ProjectCategory.GOODS
        );
        entityManager.persist(project);
        return project;
    }

    private ProjectItem item(Project project, String name, int price) {
        ProjectItem item = new ProjectItem(
                project,
                name,
                null,
                "설명",
                price,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                null,
                null,
                null,
                null,
                10
        );
        entityManager.persist(item);
        return item;
    }

    private Order order(
            String orderNo,
            Project representativeProject,
            long projectOrderNo,
            OrderStatus status,
            int finalAmount
    ) {
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order(
                orderNo,
                representativeProject,
                projectOrderNo,
                status,
                finalAmount,
                0,
                finalAmount,
                now.plusDays(1),
                "입금자",
                true,
                now,
                true,
                now,
                true,
                now
        );
        entityManager.persist(order);
        return order;
    }

    private record TestData(Project firstProject, Project secondProject, Order paidOrder, Order pendingOrder) {
    }
}
