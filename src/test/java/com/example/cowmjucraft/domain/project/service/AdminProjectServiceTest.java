package com.example.cowmjucraft.domain.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectCreateRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectUpdateRequestDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectResponseDto;
import com.example.cowmjucraft.domain.project.entity.ProjectCategory;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import com.example.cowmjucraft.domain.project.exception.ProjectException;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectItemRepository projectItemRepository;
    @Mock
    private S3PresignFacade s3PresignFacade;
    @Mock
    private ProjectLaunchNoticePublisher launchNoticePublisher;

    private AdminProjectService adminProjectService;

    @BeforeEach
    void setUp() {
        adminProjectService = new AdminProjectService(
                projectRepository,
                projectItemRepository,
                s3PresignFacade,
                launchNoticePublisher
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
    void delete_프로젝트와_하위상품_모두softDelete되고_물리삭제하지않는다() {
        // given
        Project project = project(1L);
        ProjectItem item = projectItem(project, 10L);
        given(projectRepository.findById(1L)).willReturn(Optional.of(project));
        given(projectItemRepository.findByProjectId(1L)).willReturn(List.of(item));

        // when
        adminProjectService.delete(1L);

        // then: 프로젝트와 소속 상품 모두 soft delete되고, 어떤 물리 삭제/S3 삭제도 일어나지 않는다.
        assertThat(project.isDeleted()).isTrue();
        assertThat(item.isDeleted()).isTrue();
        verify(projectRepository, never()).delete(any());
        verifyNoInteractions(s3PresignFacade);
    }

    @Test
    void delete_이미삭제된프로젝트_admin에게도숨겨져_ProjectException발생() {
        // given
        Project project = project(1L);
        project.softDelete(java.time.LocalDateTime.now());
        given(projectRepository.findById(1L)).willReturn(Optional.of(project));

        // when & then — soft delete된 프로젝트는 admin 단건 접근에서도 NOT_FOUND로 숨긴다.
        assertThatThrownBy(() -> adminProjectService.delete(1L))
                .isInstanceOf(ProjectException.class);
        verify(projectItemRepository, never()).findByProjectId(any());
    }

    @Test
    void getProject_soft삭제된프로젝트_admin에게도_ProjectException발생() {
        // given
        Project project = project(1L);
        project.softDelete(java.time.LocalDateTime.now());
        given(projectRepository.findById(1L)).willReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> adminProjectService.getProject(1L))
                .isInstanceOf(ProjectException.class);
    }

    @Test
    void create_OPEN생성_출시공지발행기에_이전상태null과_요청값전달() {
        // given
        AdminProjectCreateRequestDto request = new AdminProjectCreateRequestDto(
                "제목", "요약", "설명", "thumb.png", List.of(),
                LocalDate.now().plusDays(7), ProjectStatus.OPEN, ProjectCategory.GOODS, null
        );
        given(projectRepository.save(any(Project.class))).willAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 10L);
            return saved;
        });

        // when
        adminProjectService.create(request);

        // then
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(launchNoticePublisher).publishIfLaunched(captor.capture(), isNull(), isNull());
        assertThat(captor.getValue().getId()).isEqualTo(10L);
    }

    @Test
    void update_PREPARING에서OPEN전환_이전상태PREPARING으로_발행기호출() {
        // given
        Project project = project(1L);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.PREPARING);
        given(projectRepository.findById(1L)).willReturn(Optional.of(project));
        AdminProjectUpdateRequestDto request = new AdminProjectUpdateRequestDto(
                "제목", "요약", "설명", "thumb.png", List.of(),
                LocalDate.now().plusDays(7), ProjectStatus.OPEN, ProjectCategory.GOODS, false
        );

        // when
        adminProjectService.update(1L, request);

        // then
        verify(launchNoticePublisher).publishIfLaunched(project, ProjectStatus.PREPARING, false);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.OPEN);
    }

    @Test
    void getProject_출시공지ID_응답에포함() {
        // given
        Project project = project(1L);
        project.markLaunchNoticePublished(77L);
        given(projectRepository.findById(1L)).willReturn(Optional.of(project));

        // when
        AdminProjectResponseDto response = adminProjectService.getProject(1L);

        // then
        assertThat(response.launchNoticeId()).isEqualTo(77L);
    }

    private ProjectItem projectItem(Project project, Long id) {
        ProjectItem item = new ProjectItem(
                project,
                "테스트 상품",
                "요약",
                "설명",
                10000,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                "thumb.png",
                null,
                null,
                null,
                10
        );
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }
}
