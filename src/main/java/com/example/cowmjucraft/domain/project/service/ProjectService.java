package com.example.cowmjucraft.domain.project.service;

import com.example.cowmjucraft.domain.project.dto.response.ProjectDetailResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.ProjectListItemResponseDto;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final S3PresignFacade s3PresignFacade;

    public ProjectService(ProjectRepository projectRepository, S3PresignFacade s3PresignFacade) {
        this.projectRepository = projectRepository;
        this.s3PresignFacade = s3PresignFacade;
    }

    @Transactional(readOnly = true)
    public List<ProjectListItemResponseDto> getProjects() {
        List<Project> projects = projectRepository.findAllOrderedForPublic();
        Set<String> keySet = new LinkedHashSet<>();
        for (Project project : projects) {
            addIfValidKey(keySet, project.getThumbnailKey());
        }
        Map<String, String> urls = presignGetSafely(keySet);

        return projects.stream()
                .map(project -> new ProjectListItemResponseDto(
                        project.getId(),
                        project.getTitle(),
                        project.getSummary(),
                        project.getThumbnailKey(),
                        resolveUrl(urls, project.getThumbnailKey()),
                        project.getStatus(),
                        project.getCategory(),
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

        Set<String> keySet = new LinkedHashSet<>();
        addIfValidKey(keySet, project.getThumbnailKey());
        addIfValidKey(keySet, project.getImageKeys());
        Map<String, String> urls = presignGetSafely(keySet);

        return new ProjectDetailResponseDto(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getDescription(),
                project.getThumbnailKey(),
                resolveUrl(urls, project.getThumbnailKey()),
                project.getImageKeys(),
                buildUrlsForKeys(project.getImageKeys(), urls),
                project.getStatus(),
                project.getCategory(),
                project.getDeadlineDate(),
                calculateDDay(project.getDeadlineDate()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private long calculateDDay(LocalDate deadlineDate) {
        return ChronoUnit.DAYS.between(LocalDate.now(), deadlineDate);
    }

    private Map<String, String> presignGetSafely(Set<String> keys) {
        try {
            return keys == null || keys.isEmpty()
                    ? Map.of()
                    : s3PresignFacade.presignGet(new ArrayList<>(keys));
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String resolveUrl(Map<String, String> urls, String key) {
        String k = toNonBlankString(key);
        return k == null ? null : urls.get(k);
    }

    private List<String> buildUrlsForKeys(List<String> keys, Map<String, String> urls) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            String normalized = toNonBlankString(key);
            result.add(normalized == null ? null : urls.get(normalized));
        }
        return result;
    }

    private void addIfValidKey(Set<String> keys, String value) {
        String k = toNonBlankString(value);
        if (k != null) {
            keys.add(k);
        }
    }

    private void addIfValidKey(Set<String> keys, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            addIfValidKey(keys, value);
        }
    }

    private String toNonBlankString(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
