package com.example.cowmjucraft.domain.project.service;

import com.example.cowmjucraft.domain.project.dto.response.ProjectDetailResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.ProjectListItemResponseDto;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectListItemResponseDto> getProjects() {
        List<Project> projects = projectRepository.findAllOrderedForPublic();
        return projects.stream()
                .map(project -> new ProjectListItemResponseDto(
                        project.getId(),
                        project.getTitle(),
                        project.getSummary(),
                        project.getThumbnailKey(),
                        project.getStatus(),
                        project.getDeadlineDate(),
                        calculateDDay(project.getDeadlineDate()),
                        project.isPinned()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponseDto getProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "project not found"));

        return new ProjectDetailResponseDto(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getDescription(),
                project.getThumbnailKey(),
                project.getStatus(),
                project.getDeadlineDate(),
                calculateDDay(project.getDeadlineDate()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private long calculateDDay(LocalDate deadlineDate) {
        return ChronoUnit.DAYS.between(LocalDate.now(), deadlineDate);
    }
}
