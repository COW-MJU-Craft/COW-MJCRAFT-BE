package com.example.cowmjucraft.domain.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.cowmjucraft.domain.item.repository.ItemImageRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.payout.repository.PayoutRepository;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectResponseDto;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import com.example.cowmjucraft.domain.project.exception.ProjectException;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectItemRepository projectItemRepository;
    @Mock
    private ItemImageRepository itemImageRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PayoutRepository payoutRepository;
    @Mock
    private S3PresignFacade s3PresignFacade;

    private AdminProjectService adminProjectService;

    @BeforeEach
    void setUp() {
        adminProjectService = new AdminProjectService(
                projectRepository,
                projectItemRepository,
                itemImageRepository,
                orderItemRepository,
                orderRepository,
                payoutRepository,
                s3PresignFacade
        );
    }

    @Test
    void getProjects_OPEN필터_상태별기존정렬쿼리사용() {
        // given
        given(projectRepository.findAllByStatusOrderedForPublic(ProjectStatus.OPEN)).willReturn(List.of());

        // when
        List<AdminProjectResponseDto> response = adminProjectService.getProjects(ProjectStatus.OPEN);

        // then
        assertThat(response).isEmpty();
        verify(projectRepository).findAllByStatusOrderedForPublic(ProjectStatus.OPEN);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void getProjects_상태필터없음_기존전체정렬쿼리사용() {
        // given
        given(projectRepository.findAllOrderedForPublic()).willReturn(List.of());

        // when
        List<AdminProjectResponseDto> response = adminProjectService.getProjects();

        // then
        assertThat(response).isEmpty();
        verify(projectRepository).findAllOrderedForPublic();
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void delete_대표주문이있는프로젝트_ProjectException발생() {
        // given
        given(projectRepository.findById(1L)).willReturn(Optional.of(project(1L)));
        given(orderRepository.existsByRepresentativeProjectId(1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminProjectService.delete(1L))
                .isInstanceOf(ProjectException.class);
        verifyNoMoreInteractions(projectItemRepository, itemImageRepository, payoutRepository, s3PresignFacade);
    }
}
