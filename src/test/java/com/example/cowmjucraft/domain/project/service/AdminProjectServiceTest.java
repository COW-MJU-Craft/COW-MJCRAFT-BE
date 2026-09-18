package com.example.cowmjucraft.domain.project.service;

import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectResponseDto;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
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
    private S3PresignFacade s3PresignFacade;

    private AdminProjectService adminProjectService;

    @BeforeEach
    void setUp() {
        adminProjectService = new AdminProjectService(
                projectRepository,
                projectItemRepository,
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
    void delete_프로젝트와_하위상품을_archive처리한다() {
        // given
        Project project = project(1L);
        ProjectItem item = new ProjectItem(
                project,
                "item",
                "summary",
                "description",
                10_000,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                "thumb.png",
                null,
                null,
                null,
                10
        );
        given(projectRepository.findById(1L)).willReturn(Optional.of(project));
        given(projectItemRepository.findByProjectIdAndArchivedAtIsNull(1L)).willReturn(List.of(item));

        // when
        adminProjectService.delete(1L);

        // then
        assertThat(project.isArchived()).isTrue();
        assertThat(item.isArchived()).isTrue();
        verify(projectItemRepository).findByProjectIdAndArchivedAtIsNull(1L);
        verifyNoMoreInteractions(s3PresignFacade);
    }
}
