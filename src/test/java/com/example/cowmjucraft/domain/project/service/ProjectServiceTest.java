package com.example.cowmjucraft.domain.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.cowmjucraft.domain.project.dto.response.ProjectListItemResponseDto;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectCategory;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import com.example.cowmjucraft.domain.project.exception.ProjectException;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
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
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private S3PresignFacade s3PresignFacade;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, s3PresignFacade);
    }

    @Test
    void getProjects_삭제제외공개정렬쿼리를사용한다() {
        // given
        given(projectRepository.findAllOrderedForPublic()).willReturn(List.of());

        // when
        List<ProjectListItemResponseDto> response = projectService.getProjects();

        // then
        assertThat(response).isEmpty();
        verify(projectRepository).findAllOrderedForPublic();
    }

    @Test
    void getProject_soft삭제된프로젝트_ProjectException발생() {
        // given
        Project deleted = project(1L);
        deleted.softDelete(LocalDateTime.now());
        given(projectRepository.findById(1L)).willReturn(Optional.of(deleted));

        // when & then — 삭제된 프로젝트는 사용자에게 404로 감춰진다.
        assertThatThrownBy(() -> projectService.getProject(1L))
                .isInstanceOf(ProjectException.class);
    }

    private Project project(Long id) {
        Project project = new Project(
                "프로젝트",
                "요약",
                "설명",
                "thumb.png",
                List.of(),
                LocalDate.now().plusDays(7),
                ProjectStatus.OPEN,
                ProjectCategory.GOODS
        );
        ReflectionTestUtils.setField(project, "id", id);
        return project;
    }
}
